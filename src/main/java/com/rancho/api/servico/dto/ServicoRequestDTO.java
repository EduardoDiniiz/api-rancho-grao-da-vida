package com.rancho.api.servico.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoRequestDTO(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String nome,

        String descricao,

        @NotNull(message = "Valor padrao e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Valor invalido")
        BigDecimal valorPadrao
) {}
