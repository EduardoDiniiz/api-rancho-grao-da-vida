package com.rancho.api.exame.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ExameRequestDTO(
        @NotNull(message = "Animal e obrigatorio")
        Long animalId,

        @NotBlank(message = "Nome do exame e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String nome,

        @NotNull(message = "Data do exame e obrigatoria")
        @PastOrPresent(message = "Data do exame nao pode ser futura")
        LocalDate data,

        String resultado,

        @Size(max = 255, message = "Veterinario deve ter no maximo 255 caracteres")
        String veterinario,

        String observacao
) {}
