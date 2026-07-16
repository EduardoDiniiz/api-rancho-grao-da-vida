package com.rancho.api.pix.dto;

/** Resultado da tentativa de pagamento com cartao (checkout transparente). */
public record CartaoResponseDTO(
        boolean pago,
        String status,
        String mensagem
) {}
