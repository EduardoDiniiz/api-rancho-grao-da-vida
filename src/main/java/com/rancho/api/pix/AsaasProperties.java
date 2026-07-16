package com.rancho.api.pix;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Configuracao da integracao PIX via Asaas. As credenciais ficam apenas no
 * servidor (variaveis de ambiente). Enquanto {@code enabled=false} ou sem
 * apiKey, o sistema opera em modo mock (gera QR/copia-e-cola simulado e permite
 * simular o pagamento), util para desenvolvimento sem conta no Asaas.
 */
@Component
@ConfigurationProperties(prefix = "asaas")
@Getter
@Setter
public class AsaasProperties {

    /** Liga a integracao real. Enquanto false, usa modo mock/sandbox local. */
    private boolean enabled = false;

    /** Base da API. Sandbox: https://api-sandbox.asaas.com/v3 · Producao: https://api.asaas.com/v3 */
    private String baseUrl = "https://api-sandbox.asaas.com/v3";

    /** Chave de API (access_token) gerada no painel do Asaas. */
    private String apiKey;

    /** Token esperado no header asaas-access-token do webhook (defina o mesmo no painel). */
    private String webhookToken;

    /** Multa por atraso, em % (aplicada uma vez apos o vencimento). */
    private BigDecimal multaPercent = BigDecimal.ONE;

    /** Juros de mora, em % ao mes. */
    private BigDecimal jurosPercent = BigDecimal.ONE;
}
