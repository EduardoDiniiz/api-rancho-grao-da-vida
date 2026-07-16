package com.rancho.api.pix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.pix.dto.CartaoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente HTTP da API do Asaas. Autentica via header {@code access_token} e
 * expoe as operacoes necessarias para cobranca PIX (cliente, cobranca e QR Code).
 *
 * Doc: https://docs.asaas.com/
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AsaasClient {

    private final AsaasProperties props;
    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.create();

    @jakarta.annotation.PostConstruct
    void logModo() {
        if (isConfigured()) {
            log.info("PIX/Asaas em modo REAL (baseUrl={})", props.getBaseUrl());
        } else {
            log.warn("PIX/Asaas em modo MOCK (enabled={}, apiKey {}). Cobrancas PIX sao simuladas.",
                    props.isEnabled(), StringUtils.hasText(props.getApiKey()) ? "presente" : "ausente");
        }
    }

    public boolean isConfigured() {
        return props.isEnabled() && StringUtils.hasText(props.getApiKey());
    }

    /** Cria (ou reutiliza) um cliente no Asaas e retorna o id. Telefone e email
     *  habilitam as notificacoes (inclusive WhatsApp) do Asaas. */
    public String criarCliente(String nome, String cpfCnpj, String telefone, String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", nome);
        body.put("cpfCnpj", cpfCnpj == null ? "" : cpfCnpj.replaceAll("\\D", ""));
        if (StringUtils.hasText(telefone)) {
            body.put("mobilePhone", telefone.replaceAll("\\D", ""));
        }
        if (StringUtils.hasText(email)) {
            body.put("email", email);
        }
        return post("/customers", body).path("id").asText(null);
    }

    /**
     * Simula a taxa do Asaas para um valor e billingType e devolve a taxa
     * (value - netValue). Usado para repassar a taxa real ao cliente.
     * Doc: POST /payments/simulate (Simulador de vendas).
     */
    public java.math.BigDecimal simularTaxa(java.math.BigDecimal valor, String billingType) {
        JsonNode resp = post("/payments/simulate", Map.of(
                "value", valor,
                "billingTypes", java.util.List.of(billingType)));
        log.info("Asaas simulate ({}, {}): {}", valor, billingType, resp);
        java.math.BigDecimal net = buscarNetValue(resp, billingType);
        if (net == null || net.signum() <= 0) {
            log.warn("Asaas simulate sem netValue para {}; taxa=0. Resposta: {}", billingType, resp);
            return java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal taxa = valor.subtract(net);
        return taxa.signum() > 0 ? taxa : java.math.BigDecimal.ZERO;
    }

    /** Procura o netValue do billingType na resposta do simulador (formato defensivo). */
    private java.math.BigDecimal buscarNetValue(JsonNode node, String billingType) {
        if (node == null) return null;
        if (node.isObject()) {
            JsonNode net = node.get("netValue");
            JsonNode bt = node.get("billingType");
            if (net != null && net.isNumber()
                    && (bt == null || billingType.equalsIgnoreCase(bt.asText()))) {
                return net.decimalValue();
            }
            for (JsonNode child : node) {
                java.math.BigDecimal found = buscarNetValue(child, billingType);
                if (found != null) return found;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                java.math.BigDecimal found = buscarNetValue(child, billingType);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Cria uma cobranca no Asaas (billingType PIX ou CREDIT_CARD) com multa e juros. */
    public JsonNode criarCobranca(String customerId, String billingType, java.math.BigDecimal valor,
                                  java.time.LocalDate vencimento, String descricao, String externalReference,
                                  java.math.BigDecimal multaPercent, java.math.BigDecimal jurosPercent) {
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("billingType", billingType);
        body.put("value", valor);
        body.put("dueDate", vencimento.toString());
        body.put("description", descricao == null ? "" : descricao);
        body.put("externalReference", externalReference);
        if (multaPercent != null && multaPercent.signum() > 0) {
            body.put("fine", Map.of("value", multaPercent, "type", "PERCENTAGE"));
        }
        if (jurosPercent != null && jurosPercent.signum() > 0) {
            body.put("interest", Map.of("value", jurosPercent));
        }
        return post("/payments", body);
    }

    /**
     * Cria uma cobranca no cartao (checkout transparente) com autorizacao imediata.
     * Os dados do cartao trafegam apenas de passagem para o Asaas; nunca sao gravados.
     */
    public JsonNode criarCobrancaCartao(String customerId, java.math.BigDecimal valor,
                                        java.time.LocalDate vencimento, String descricao, String externalReference,
                                        java.math.BigDecimal multaPercent, java.math.BigDecimal jurosPercent,
                                        CartaoRequest c, String remoteIp) {
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("billingType", "CREDIT_CARD");
        body.put("value", valor);
        body.put("dueDate", vencimento.toString());
        body.put("description", descricao == null ? "" : descricao);
        body.put("externalReference", externalReference);
        body.put("remoteIp", remoteIp);
        if (multaPercent != null && multaPercent.signum() > 0) {
            body.put("fine", Map.of("value", multaPercent, "type", "PERCENTAGE"));
        }
        if (jurosPercent != null && jurosPercent.signum() > 0) {
            body.put("interest", Map.of("value", jurosPercent));
        }
        body.put("creditCard", Map.of(
                "holderName", c.holderName(),
                "number", c.number().replaceAll("\\s", ""),
                "expiryMonth", c.expiryMonth(),
                "expiryYear", c.expiryYear(),
                "ccv", c.ccv()));
        Map<String, Object> holder = new HashMap<>();
        holder.put("name", c.holderName());
        holder.put("cpfCnpj", c.cpfCnpj().replaceAll("\\D", ""));
        holder.put("postalCode", c.postalCode().replaceAll("\\D", ""));
        holder.put("addressNumber", c.addressNumber());
        holder.put("phone", c.phone().replaceAll("\\D", ""));
        if (StringUtils.hasText(c.email())) {
            holder.put("email", c.email());
        }
        body.put("creditCardHolderInfo", holder);
        return post("/payments", body);
    }

    /** Cancela (remove) uma cobranca no Asaas; usado ao recriar por troca de metodo. */
    public void cancelarCobranca(String paymentId) {
        try {
            request(HttpMethod.DELETE, "/payments/" + paymentId, null);
        } catch (Exception e) {
            log.warn("Falha ao cancelar cobranca Asaas {} (ignorado): {}", paymentId, e.getMessage());
        }
    }

    /** Busca o QR Code PIX (encodedImage base64 + payload copia-e-cola) de uma cobranca. */
    public JsonNode obterQrCodePix(String paymentId) {
        return request(HttpMethod.GET, "/payments/" + paymentId + "/pixQrCode", null);
    }

    private JsonNode post(String path, Object body) {
        return request(HttpMethod.POST, path, body);
    }

    private JsonNode request(HttpMethod method, String path, Object bodyObj) {
        if (!isConfigured()) {
            throw new BusinessException("Integracao Asaas nao configurada.");
        }
        try {
            RestClient.RequestBodySpec spec = restClient.method(method)
                    .uri(props.getBaseUrl() + path)
                    .header("access_token", props.getApiKey())
                    .header("Content-Type", "application/json");

            String raw = (bodyObj != null)
                    ? spec.body(objectMapper.writeValueAsString(bodyObj)).retrieve().body(String.class)
                    : spec.retrieve().body(String.class);

            JsonNode resp = objectMapper.readTree(raw == null ? "{}" : raw);
            if (resp.has("errors")) {
                String msg = resp.path("errors").path(0).path("description").asText("erro desconhecido");
                log.warn("Asaas respondeu erro: {}", msg);
                throw new BusinessException("Asaas: " + msg);
            }
            return resp;
        } catch (BusinessException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            // 4xx do Asaas (ex.: cartao recusado, dados invalidos): extrai a descricao real.
            String msg = extrairErro(e.getResponseBodyAsString());
            log.warn("Asaas {} {} respondeu {}: {}", method, path, e.getStatusCode(), msg);
            throw new BusinessException(msg != null ? "Asaas: " + msg : "Falha na comunicacao com o Asaas.");
        } catch (Exception e) {
            log.error("Falha na chamada ao Asaas {} {}", method, path, e);
            throw new BusinessException("Falha na comunicacao com o Asaas.");
        }
    }

    /** Extrai a descricao do erro do corpo JSON do Asaas ({"errors":[{"description":...}]}). */
    private String extrairErro(String body) {
        try {
            JsonNode node = objectMapper.readTree(body == null ? "{}" : body);
            JsonNode desc = node.path("errors").path(0).path("description");
            return desc.isMissingNode() || desc.asText().isBlank() ? null : desc.asText();
        } catch (Exception e) {
            return null;
        }
    }
}
