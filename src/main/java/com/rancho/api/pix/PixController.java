package com.rancho.api.pix;

import com.fasterxml.jackson.databind.JsonNode;
import com.rancho.api.pix.dto.PixResponseDTO;
import com.rancho.api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PixController {

    private final PixService pixService;

    /** Gera/recupera o PIX de uma cobranca (dono da cobranca, operador ou admin). */
    @PostMapping("/v1/pagamentos/{id}/pix")
    public ResponseEntity<PixResponseDTO> gerarPix(@PathVariable Long id,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pixService.gerarPix(id, user));
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
