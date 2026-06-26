package com.rancho.api.dispositivo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rancho.api.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Cliente HTTP da Tuya Cloud API. Cuida da assinatura HMAC-SHA256 exigida pela Tuya,
 * faz cache do access_token e expoe GET/POST autenticados.
 *
 * Doc da assinatura: https://developer.tuya.com/en/docs/iot/api-request
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TuyaClient {

    /** SHA-256 de corpo vazio (usado em requisicoes sem body, como GET). */
    private static final String EMPTY_BODY_SHA256 =
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final String SIGN_METHOD = "HMAC-SHA256";

    private final TuyaProperties props;
    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.create();

    private volatile String cachedToken;
    private volatile long tokenExpiresAt; // epoch ms

    public boolean isConfigured() {
        return props.isEnabled()
                && StringUtils.hasText(props.getAccessId())
                && StringUtils.hasText(props.getAccessSecret());
    }

    public JsonNode get(String path) {
        return request(HttpMethod.GET, path, null);
    }

    public JsonNode post(String path, Object body) {
        return request(HttpMethod.POST, path, body);
    }

    // ---- token ----

    private synchronized String getToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpiresAt - 60_000) {
            return cachedToken;
        }
        String path = "/v1.0/token?grant_type=1";
        long t = System.currentTimeMillis();
        String stringToSign = buildStringToSign("GET", path, "");
        String sign = hmacSha256(props.getAccessId() + t + stringToSign);

        String raw = restClient.get()
                .uri(props.getEndpoint() + path)
                .header("client_id", props.getAccessId())
                .header("sign", sign)
                .header("t", String.valueOf(t))
                .header("sign_method", SIGN_METHOD)
                .retrieve()
                .body(String.class);

        JsonNode resp = parse(raw);
        JsonNode result = resp.path("result");
        cachedToken = result.path("access_token").asText(null);
        long expireSeconds = result.path("expire_time").asLong(7200);
        tokenExpiresAt = System.currentTimeMillis() + expireSeconds * 1000;
        if (cachedToken == null) {
            throw new BusinessException("Falha ao autenticar na Tuya. Verifique as credenciais.");
        }
        return cachedToken;
    }

    // ---- request generico autenticado ----

    private JsonNode request(HttpMethod method, String path, Object bodyObj) {
        if (!isConfigured()) {
            throw new BusinessException("Integracao Tuya nao configurada.");
        }
        String token = getToken();
        long t = System.currentTimeMillis();
        String body = "";
        try {
            if (bodyObj != null) {
                body = objectMapper.writeValueAsString(bodyObj);
            }
        } catch (Exception e) {
            throw new BusinessException("Erro ao serializar comando para a Tuya.");
        }

        String stringToSign = buildStringToSign(method.name(), path, body);
        String sign = hmacSha256(props.getAccessId() + token + t + stringToSign);

        RestClient.RequestBodySpec spec = restClient.method(method)
                .uri(props.getEndpoint() + path)
                .header("client_id", props.getAccessId())
                .header("access_token", token)
                .header("sign", sign)
                .header("t", String.valueOf(t))
                .header("sign_method", SIGN_METHOD);

        String raw;
        if (!body.isEmpty()) {
            raw = spec.header("Content-Type", "application/json").body(body).retrieve().body(String.class);
        } else {
            raw = spec.retrieve().body(String.class);
        }

        JsonNode resp = parse(raw);
        if (!resp.path("success").asBoolean(false)) {
            String msg = resp.path("msg").asText("erro desconhecido");
            log.warn("Tuya respondeu erro: code={} msg={}", resp.path("code").asInt(), msg);
            throw new BusinessException("Tuya: " + msg);
        }
        return resp.path("result");
    }

    // ---- assinatura ----

    private String buildStringToSign(String method, String path, String body) {
        String contentSha256 = (body == null || body.isEmpty())
                ? EMPTY_BODY_SHA256 : sha256Hex(body);
        // method \n Content-SHA256 \n (headers vazio) \n url
        return method + "\n" + contentSha256 + "\n" + "\n" + path;
    }

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    props.getAccessSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes).toUpperCase();
        } catch (Exception e) {
            throw new BusinessException("Erro ao assinar requisicao Tuya.");
        }
    }

    private String sha256Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException("Erro ao calcular hash do comando Tuya.");
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private JsonNode parse(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new BusinessException("Resposta invalida da Tuya.");
        }
    }
}
