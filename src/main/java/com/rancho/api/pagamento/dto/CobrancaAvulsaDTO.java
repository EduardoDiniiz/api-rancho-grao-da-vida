package com.rancho.api.pagamento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobrancaAvulsaDTO(
        Long animalId,

        @NotBlank(message = "Descricao e obrigatoria")
        @Size(min = 2, max = 255, message = "Descricao deve ter entre 2 e 255 caracteres")
        String descricao,

        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Valor invalido")
        BigDecimal valor,

        @NotNull(message = "Vencimento e obrigatorio")
        LocalDate vencimento
) {}
