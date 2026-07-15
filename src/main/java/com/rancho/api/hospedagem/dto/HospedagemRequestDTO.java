package com.rancho.api.hospedagem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HospedagemRequestDTO(
        @NotNull(message = "Animal e obrigatorio")
        Long animalId,

        @NotNull(message = "Baia e obrigatoria")
        Long baiaId,

        @NotNull(message = "Data de entrada e obrigatoria")
        LocalDate dataEntrada,

        // Opcional: quando informado (> 0) gera cobranca mensal recorrente
        @PositiveOrZero(message = "Valor mensal nao pode ser negativo")
        BigDecimal valorMensal
) {}
