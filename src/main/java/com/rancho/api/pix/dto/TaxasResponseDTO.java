package com.rancho.api.pix.dto;

import java.math.BigDecimal;

/**
 * Simulacao das taxas por metodo para a tela de escolha (PIX x Cartao), com o
 * valor total que o cliente pagaria em cada um.
 */
public record TaxasResponseDTO(
        BigDecimal valorBase,
        TaxaDTO pix,
        TaxaDTO cartao
) {}
