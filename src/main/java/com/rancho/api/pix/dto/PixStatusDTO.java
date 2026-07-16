package com.rancho.api.pix.dto;

import com.rancho.api.pagamento.PagamentoStatus;

/**
 * Status atual de uma cobranca, usado pelo modal de PIX para detectar a
 * confirmacao do pagamento (via polling) e exibir a mensagem de sucesso.
 */
public record PixStatusDTO(
        Long pagamentoId,
        PagamentoStatus status
) {}
