package com.rancho.api.pix;

import com.fasterxml.jackson.databind.JsonNode;
import com.rancho.api.pix.dto.CartaoRequest;
import com.rancho.api.pix.dto.CartaoResponseDTO;
import com.rancho.api.pix.dto.PixResponseDTO;
import com.rancho.api.pix.dto.PixStatusDTO;
import com.rancho.api.pix.dto.TaxasResponseDTO;
import com.rancho.api.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PixController {

    private final PixService pixService;

    /** Simula as taxas de PIX e cartao (tela de escolha do metodo de pagamento). */
    @GetMapping("/v1/pagamentos/{id}/taxas")
    public ResponseEntity<TaxasResponseDTO> taxas(@PathVariable Long id,
                                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pixService.consultarTaxas(id, user));
    }

    /** Gera/recupera o PIX de uma cobranca (dono da cobranca, operador ou admin). */
    @PostMapping("/v1/pagamentos/{id}/pix")
    public ResponseEntity<PixResponseDTO> gerarPix(@PathVariable Long id,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pixService.gerarPix(id, user));
    }

    /** Paga a cobranca com cartao de credito (checkout transparente). */
    @PostMapping("/v1/pagamentos/{id}/cartao")
    public ResponseEntity<CartaoResponseDTO> cartao(@PathVariable Long id,
                                                    @Valid @RequestBody CartaoRequest req,
                                                    HttpServletRequest http,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pixService.gerarCartao(id, req, extrairIp(http), user));
    }

    /** IP real do cliente (atras do proxy Caddy/nginx), exigido pelo Asaas no cartao. */
    private String extrairIp(HttpServletRequest http) {
        String xff = http.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }

    /** Consulta o status da cobranca (polling do modal ate a confirmacao do pagamento). */
    @GetMapping("/v1/pagamentos/{id}/pix/status")
    public ResponseEntity<PixStatusDTO> status(@PathVariable Long id,
                                               @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pixService.consultarStatus(id, user));
    }

    /** Simula a confirmacao do PIX (apenas no modo mock/sandbox). */
    @PostMapping("/v1/pagamentos/{id}/pix/simular")
    public ResponseEntity<Void> simular(@PathVariable Long id,
                                        @AuthenticationPrincipal User user) {
        pixService.simularPagamento(id, user);
        return ResponseEntity.noContent().build();
    }

    /** Webhook publico do Asaas (validado por token no header). */
    @PostMapping("/v1/webhooks/asaas")
    public ResponseEntity<Void> webhook(@RequestBody JsonNode body,
                                        @RequestHeader(value = "asaas-access-token", required = false) String token) {
        pixService.processarWebhook(body, token);
        return ResponseEntity.ok().build();
    }
}
