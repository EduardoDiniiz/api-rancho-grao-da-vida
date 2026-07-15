package com.rancho.api.pix.dto;

import com.rancho.api.pagamento.PagamentoStatus;

import java.math.BigDecimal;

/**
 * Dados do PIX de uma cobranca para exibicao na tela de faturamento.
 *
 * @param encodedImage QR Code em PNG base64 (presente no modo real do Asaas; nulo no mock)
 * @param payload      copia-e-cola do PIX
 * @param mock         true quando gerado em modo simulado (sem integracao Asaas ativa)
 */
public record PixResponseDTO(
        Long pagamentoId,
        BigDecimal valor,
        PagamentoStatus status,
        String encodedImage,
        String payload,
        boolean mock
) {}
