package com.rancho.api.animalservico.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalServicoRequestDTO(
        @NotNull(message = "Animal e obrigatorio")
        Long animalId,

        @NotBlank(message = "Servico e obrigatorio")
        @Size(max = 255, message = "Servico deve ter no maximo 255 caracteres")
        String servicoNome,

        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Valor invalido")
        BigDecimal valor,

        @NotNull(message = "Data de inicio e obrigatoria")
        LocalDate dataInicio,

        @NotNull(message = "Recorrencia em dias e obrigatoria")
        @Min(value = 1, message = "Recorrencia minima e 1 dia")
        Integer recorrenciaDias,

        String descricao
) {}
