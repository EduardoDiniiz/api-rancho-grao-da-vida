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
import com.rancho.api.pix.dto.CartaoRequest;
import com.rancho.api.pix.dto.CartaoResponseDTO;
import com.rancho.api.pix.dto.PixResponseDTO;
import com.rancho.api.pix.dto.PixStatusDTO;
import com.rancho.api.pix.dto.TaxaDTO;
import com.rancho.api.pix.dto.TaxasResponseDTO;
import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixService {

    private static final Set<String> EVENTOS_PAGO = Set.of("PAYMENT_RECEIVED", "PAYMENT_CONFIRMED");

    /** Status do Asaas que indicam pagamento efetivado. */
    private static final Set<String> STATUS_PAGO = Set.of("CONFIRMED", "RECEIVED", "RECEIVED_IN_CASH");

    /** Prefixo do id gerado no modo mock; nao corresponde a uma cobranca real no Asaas. */
    private static final String MOCK_PREFIX = "MOCK-";

    private final PagamentoRepository pagamentoRepository;
    private final ClienteRepository clienteRepository;
    private final PagamentoService pagamentoService;
    private final AsaasClient asaasClient;
    private final AsaasProperties props;

    /**
     * Simula as taxas de PIX e cartao para a tela de escolha do metodo, com o
     * valor total (base + taxa) que o cliente pagaria em cada um.
     */
    @Transactional(readOnly = true)
    public TaxasResponseDTO consultarTaxas(Long pagamentoId, User requester) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", pagamentoId));
        verificarPropriedade(pagamento, requester);
        BigDecimal base = pagamento.getValor();
        if (!asaasClient.isConfigured()) {
            TaxaDTO semTaxa = new TaxaDTO(base, BigDecimal.ZERO, base);
            return new TaxasResponseDTO(base, semTaxa, semTaxa);
        }
        BigDecimal txPix = asaasClient.simularTaxa(base, MetodoCobranca.PIX.asaasBillingType());
        BigDecimal txCartao = asaasClient.simularTaxa(base, MetodoCobranca.CARTAO.asaasBillingType());
        return new TaxasResponseDTO(base,
                new TaxaDTO(base, txPix, totalComTaxa(base, txPix)),
                new TaxaDTO(base, txCartao, totalComTaxa(base, txCartao)));
    }

    /**
     * Gera (ou recupera) o PIX de uma cobranca. No modo real usa o Asaas com a
     * taxa repassada ao cliente; sem integracao configurada devolve um PIX simulado.
     */
    @Transactional
    public PixResponseDTO gerarPix(Long pagamentoId, User requester) {
        Pagamento pagamento = carregarParaCobranca(pagamentoId, requester);
        if (!asaasClient.isConfigured()) {
            return gerarPixMock(pagamento);
        }
        String customerId = garantirCustomer(clienteDe(pagamento));

        BigDecimal base = pagamento.getValor();
        BigDecimal taxa = asaasClient.simularTaxa(base, MetodoCobranca.PIX.asaasBillingType());
        BigDecimal total = totalComTaxa(base, taxa);

        // Reaproveita a cobranca PIX existente; recria se nao existir, se for id de
        // mock, ou se a cobranca anterior era de outro metodo (valor difere).
        String asaasId = pagamento.getAsaasPaymentId();
        boolean cobrancaValida = StringUtils.hasText(asaasId) && !asaasId.startsWith(MOCK_PREFIX)
                && pagamento.getCobrancaMetodo() == MetodoCobranca.PIX;
        if (!cobrancaValida) {
            cancelarSePreciso(asaasId);
            JsonNode cobranca = asaasClient.criarCobranca(customerId, MetodoCobranca.PIX.asaasBillingType(),
                    total, pagamento.getVencimento(), pagamento.getDescricao(), String.valueOf(pagamento.getId()),
                    props.getMultaPercent(), props.getJurosPercent());
            pagamento.setAsaasPaymentId(cobranca.path("id").asText(null));
            pagamento.setCobrancaMetodo(MetodoCobranca.PIX);
            pagamentoRepository.save(pagamento);
        }

        JsonNode qr = asaasClient.obterQrCodePix(pagamento.getAsaasPaymentId());
        String payload = qr.path("payload").asText(null);
        if (StringUtils.hasText(payload)) {
            pagamento.setPixPayload(payload);
            pagamentoRepository.save(pagamento);
        }
        return new PixResponseDTO(pagamento.getId(), base, taxa, total, pagamento.getStatus(),
                qr.path("encodedImage").asText(null), payload, false);
    }

    /**
     * Paga a cobranca com cartao (checkout transparente do Asaas), com autorizacao
     * imediata. O titular do cartao pode ser diferente do cliente. Repassa a taxa
     * do cartao ao valor cobrado.
     */
    @Transactional
    public CartaoResponseDTO gerarCartao(Long pagamentoId, CartaoRequest req, String remoteIp, User requester) {
        Pagamento pagamento = carregarParaCobranca(pagamentoId, requester);
        if (!asaasClient.isConfigured()) {
            throw new BusinessException("Pagamento com cartao indisponivel no modo simulado");
        }
        String customerId = garantirCustomer(clienteDe(pagamento));

        BigDecimal base = pagamento.getValor();
        BigDecimal taxa = asaasClient.simularTaxa(base, MetodoCobranca.CARTAO.asaasBillingType());
        BigDecimal total = totalComTaxa(base, taxa);

        // Cada tentativa de cartao e uma nova cobranca (autorizacao). Remove a anterior nao paga.
        cancelarSePreciso(pagamento.getAsaasPaymentId());
        JsonNode cobranca = asaasClient.criarCobrancaCartao(customerId, total, pagamento.getVencimento(),
                pagamento.getDescricao(), String.valueOf(pagamento.getId()),
                props.getMultaPercent(), props.getJurosPercent(), req, remoteIp);
        pagamento.setAsaasPaymentId(cobranca.path("id").asText(null));
        pagamento.setCobrancaMetodo(MetodoCobranca.CARTAO);
        pagamento.setPixPayload(null);
        pagamentoRepository.save(pagamento);

        String status = cobranca.path("status").asText("");
        boolean pago = STATUS_PAGO.contains(status);
        if (pago && pagamento.getStatus() != PagamentoStatus.PAGO) {
            pagamentoService.registrarPagamento(pagamento.getId(), LocalDate.now(), FormaPagamento.CARTAO_CREDITO);
        }
        return new CartaoResponseDTO(pago, status,
                pago ? "Pagamento aprovado" : "Pagamento nao aprovado (status " + status + ")");
    }

    /**
     * Consulta o status atual de uma cobranca (para o modal detectar a confirmacao
     * do pagamento via polling). Respeita a propriedade do CLIENTE.
     */
    @Transactional(readOnly = true)
    public PixStatusDTO consultarStatus(Long pagamentoId, User requester) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", pagamentoId));
        verificarPropriedade(pagamento, requester);
        return new PixStatusDTO(pagamento.getId(), pagamento.getStatus());
    }

    private PixResponseDTO gerarPixMock(Pagamento pagamento) {
        if (!StringUtils.hasText(pagamento.getAsaasPaymentId())) {
            pagamento.setAsaasPaymentId(MOCK_PREFIX + pagamento.getId());
            pagamento.setPixPayload(
                    "00020126SIMULADO-PIX-PAGAMENTO-" + pagamento.getId()
                    + "-VALOR-" + pagamento.getValor().toPlainString() + "5204000053039865802BR6304MOCK");
            pagamento.setCobrancaMetodo(MetodoCobranca.PIX);
            pagamentoRepository.save(pagamento);
        }
        BigDecimal base = pagamento.getValor();
        return new PixResponseDTO(pagamento.getId(), base, BigDecimal.ZERO, base, pagamento.getStatus(),
                null, pagamento.getPixPayload(), true);
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
        FormaPagamento forma = "CREDIT_CARD".equals(payment.path("billingType").asText(""))
                ? FormaPagamento.CARTAO_CREDITO : FormaPagamento.PIX;

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
            pagamentoService.registrarPagamento(pagamento.getId(), LocalDate.now(), forma);
            log.info("Webhook Asaas: pagamento {} confirmado ({})", pagamento.getId(), forma);
        }
    }

    // --- helpers ---

    private Pagamento carregarParaCobranca(Long pagamentoId, User requester) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", pagamentoId));
        verificarPropriedade(pagamento, requester);
        if (pagamento.getStatus() == PagamentoStatus.PAGO) {
            throw new BusinessException("Esta cobranca ja foi paga");
        }
        if (pagamento.getStatus() == PagamentoStatus.CANCELADO) {
            throw new BusinessException("Cobranca cancelada nao pode gerar pagamento");
        }
        return pagamento;
    }

    private Cliente clienteDe(Pagamento pagamento) {
        Cliente cliente = pagamento.getAnimal() != null ? pagamento.getAnimal().getCliente() : null;
        if (cliente == null) {
            throw new BusinessException("Cobranca sem cliente vinculado; nao e possivel gerar a cobranca");
        }
        return cliente;
    }

    /** Garante um customer no Asaas para o cliente (com telefone/email p/ notificacoes). */
    private String garantirCustomer(Cliente cliente) {
        if (!StringUtils.hasText(cliente.getAsaasCustomerId())) {
            if (!StringUtils.hasText(cliente.getCpfCnpj())) {
                throw new BusinessException("Informe o CPF/CNPJ do cliente antes de gerar a cobranca");
            }
            String customerId = asaasClient.criarCliente(cliente.getNome(), cliente.getCpfCnpj(),
                    cliente.getTelefone(), cliente.getEmail());
            cliente.setAsaasCustomerId(customerId);
            clienteRepository.save(cliente);
        }
        return cliente.getAsaasCustomerId();
    }

    private void cancelarSePreciso(String asaasId) {
        if (StringUtils.hasText(asaasId) && !asaasId.startsWith(MOCK_PREFIX)) {
            asaasClient.cancelarCobranca(asaasId);
        }
    }

    private BigDecimal totalComTaxa(BigDecimal base, BigDecimal taxa) {
        return base.add(taxa == null ? BigDecimal.ZERO : taxa).setScale(2, RoundingMode.HALF_UP);
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
