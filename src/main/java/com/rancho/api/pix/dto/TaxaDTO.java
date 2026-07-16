package com.rancho.api.pix.dto;

import java.math.BigDecimal;

/** Taxa de um metodo de pagamento e o valor total resultante (base + taxa). */
public record TaxaDTO(
        BigDecimal valorBase,
        BigDecimal taxa,
        BigDecimal valorTotal
) {}
