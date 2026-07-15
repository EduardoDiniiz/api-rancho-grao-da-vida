package com.rancho.api.pix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rancho.api.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

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

    public boolean isConfigured() {
        return props.isEnabled() && StringUtils.hasText(props.getApiKey());
    }

    /** Cria (ou reutiliza) um cliente no Asaas e retorna o id. */
    public String criarCliente(String nome, String cpfCnpj) {
        JsonNode resp = post("/customers", Map.of(
                "name", nome,
                "cpfCnpj", cpfCnpj == null ? "" : cpfCnpj.replaceAll("\\D", "")));
        return resp.path("id").asText(null);
    }

    /** Cria uma cobranca PIX e retorna o corpo completo (inclui o id da cobranca). */
    public JsonNode criarCobrancaPix(String customerId, java.math.BigDecimal valor,
                                     java.time.LocalDate vencimento, String descricao,
                                     String externalReference) {
        return post("/payments", Map.of(
                "customer", customerId,
                "billingType", "PIX",
                "value", valor,
                "dueDate", vencimento.toString(),
                "description", descricao == null ? "" : descricao,
                "externalReference", externalReference));
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
        } catch (Exception e) {
            log.error("Falha na chamada ao Asaas {} {}", method, path, e);
            throw new BusinessException("Falha na comunicacao com o Asaas.");
        }
    }
}
