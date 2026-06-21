package com.rancho.api.hospedagem.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HospedagemRequestDTO(
        @NotNull(message = "Animal e obrigatorio")
        Long animalId,

        @NotNull(message = "Baia e obrigatoria")
        Long baiaId,

        @NotNull(message = "Data de entrada e obrigatoria")
        LocalDate dataEntrada
) {}
