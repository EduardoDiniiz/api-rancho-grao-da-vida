package com.rancho.api.pix;

import com.fasterxml.jackson.databind.JsonNode;
import com.rancho.api.cliente.Cliente;
import com.rancho.api.cliente.ClienteRepository;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.pagamento.FormaPagamento;
import com.rancho.api.pagamento.Pagamento;
import com.rancho.api.pagamento.PagamentoRepository;
import com.rancho.api.pagamento.PagamentoService;
import com.rancho.api.pagamento.PagamentoStatus;
import com.rancho.api.pix.dto.PixResponseDTO;
import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixService {

    private static final Set<String> EVENTOS_PAGO = Set.of("PAYMENT_RECEIVED", "PAYMENT_CONFIRMED");

    private final PagamentoRepository pagamentoRepository;
    private final ClienteRepository clienteRepository;
    private final PagamentoService pagamentoService;
    private final AsaasClient asaasClient;
    private final AsaasProperties props;

    /**
     * Gera (ou recupera) o PIX de uma cobranca. No modo real usa o Asaas; sem
     * integracao configurada devolve um PIX simulado para desenvolvimento.
     */
    @Transactional
    public PixResponseDTO gerarPix(Long pagamentoId, User requester) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", pagamentoId));
        verificarPropriedade(pagamento, requester);

        if (pagamento.getStatus() == PagamentoStatus.PAGO) {
            throw new BusinessException("Esta cobranca ja foi paga");
        }
        if (pagamento.getStatus() == PagamentoStatus.CANCELADO) {
            throw new BusinessException("Cobranca cancelada nao pode gerar PIX");
        }

        Cliente cliente = pagamento.getAnimal() != null ? pagamento.getAnimal().getCliente() : null;
        if (cliente == null) {
            throw new BusinessException("Cobranca sem cliente vinculado; nao e possivel gerar PIX");
        }

        if (!asaasClient.isConfigured()) {
            return gerarPixMock(pagamento);
        }
        return gerarPixAsaas(pagamento, cliente);
    }

    private PixResponseDTO gerarPixMock(Pagamento pagamento) {
        if (!StringUtils.hasText(pagamento.getAsaasPaymentId())) {
            pagamento.setAsaasPaymentId("MOCK-" + pagamento.getId());
            pagamento.setPixPayload(
                    "00020126SIMULADO-PIX-PAGAMENTO-" + pagamento.getId()
                    + "-VALOR-" + pagamento.getValor().toPlainString() + "5204000053039865802BR6304MOCK");
            pagamentoRepository.save(pagamento);
        }
        return new PixResponseDTO(pagamento.getId(), pagamento.getValor(), pagamento.getStatus(),
                null, pagamento.getPixPayload(), true);
    }

    private PixResponseDTO gerarPixAsaas(Pagamento pagamento, Cliente cliente) {
        // 1. Garante um customer no Asaas para o cliente
        if (!StringUtils.hasText(cliente.getAsaasCustomerId())) {
            if (!StringUtils.hasText(cliente.getCpfCnpj())) {
                throw new BusinessException("Informe o CPF/CNPJ do cliente antes de gerar o PIX");
            }
            String customerId = asaasClient.criarCliente(cliente.getNome(), cliente.getCpfCnpj());
            cliente.setAsaasCustomerId(customerId);
            clienteRepository.save(cliente);
        }

        // 2. Cria a cobranca PIX (uma vez por pagamento)
        if (!StringUtils.hasText(pagamento.getAsaasPaymentId())) {
            JsonNode cobranca = asaasClient.criarCobrancaPix(
                    cliente.getAsaasCustomerId(), pagamento.getValor(), pagamento.getVencimento(),
                    pagamento.getDescricao(), String.valueOf(pagamento.getId()));
            pagamento.setAsaasPaymentId(cobranca.path("id").asText(null));
            pagamentoRepository.save(pagamento);
        }

        // 3. Busca o QR Code
        JsonNode qr = asaasClient.obterQrCodePix(pagamento.getAsaasPaymentId());
        String payload = qr.path("payload").asText(null);
        if (StringUtils.hasText(payload)) {
            pagamento.setPixPayload(payload);
            pagamentoRepository.save(pagamento);
        }
        return new PixResponseDTO(pagamento.getId(), pagamento.getValor(), pagamento.getStatus(),
                qr.path("encodedImage").asText(null), payload, false);
    }

    /**
     * Simula a confirmacao de pagamento (apenas no modo mock/sandbox local),
     * dando baixa e disparando a recorrencia normalmente.
     */
    @Transactional
    public void simularPagamento(Long pagamentoId, User requester) {
        if (asaasClient.isConfigured()) {
            throw new BusinessException("Simulacao indisponivel com o Asaas ativo");
        }
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", pagamentoId));
        verificarPropriedade(pagamento, requester);
        if (pagamento.getStatus() != PagamentoStatus.PAGO) {
            pagamentoService.registrarPagamento(pagamentoId, LocalDate.now(), FormaPagamento.PIX);
        }
    }

    /** Processa o webhook do Asaas: em evento de pagamento recebido, da baixa. */
    @Transactional
    public void processarWebhook(JsonNode body, String token) {
        if (StringUtils.hasText(props.getWebhookToken())
                && !props.getWebhookToken().equals(token)) {
            throw new AccessDeniedException("Token de webhook invalido");
        }

        String evento = body.path("event").asText("");
        if (!EVENTOS_PAGO.contains(evento)) {
            return;
        }

        JsonNode payment = body.path("payment");
        String asaasId = payment.path("id").asText(null);
        String externalRef = payment.path("externalReference").asText(null);

        Pagamento pagamento = null;
        if (StringUtils.hasText(asaasId)) {
            pagamento = pagamentoRepository.findByAsaasPaymentId(asaasId).orElse(null);
        }
        if (pagamento == null && StringUtils.hasText(externalRef)) {
            try {
                pagamento = pagamentoRepository.findById(Long.parseLong(externalRef)).orElse(null);
            } catch (NumberFormatException ignored) {
                // externalReference nao numerico -> ignora
            }
        }

        if (pagamento == null) {
            log.warn("Webhook Asaas: pagamento nao encontrado (asaasId={}, ref={})", asaasId, externalRef);
            return;
        }
        if (pagamento.getStatus() != PagamentoStatus.PAGO) {
            pagamentoService.registrarPagamento(pagamento.getId(), LocalDate.now(), FormaPagamento.PIX);
            log.info("Webhook Asaas: pagamento {} confirmado via PIX", pagamento.getId());
        }
    }

    private void verificarPropriedade(Pagamento pagamento, User requester) {
        if (requester.getRole() != Role.CLIENTE) {
            return;
        }
        Long donoId = pagamento.getAnimal() != null && pagamento.getAnimal().getCliente() != null
                ? pagamento.getAnimal().getCliente().getId() : null;
        if (!Objects.equals(donoId, requester.getClienteId())) {
            throw new AccessDeniedException("Acesso negado a esta cobranca");
        }
    }
}
