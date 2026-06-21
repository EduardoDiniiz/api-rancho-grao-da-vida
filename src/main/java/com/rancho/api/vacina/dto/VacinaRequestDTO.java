package com.rancho.api.vacina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VacinaRequestDTO(
        @NotNull(message = "Animal e obrigatorio")
        Long animalId,

        @NotBlank(message = "Nome da vacina e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String nome,

        @NotNull(message = "Data de aplicacao e obrigatoria")
        @PastOrPresent(message = "Data de aplicacao nao pode ser futura")
        LocalDate dataAplicacao,

        LocalDate dataVencimento,

        String observacao
) {}
