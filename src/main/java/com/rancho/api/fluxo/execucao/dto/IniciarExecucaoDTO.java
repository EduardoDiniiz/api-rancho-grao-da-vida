package com.rancho.api.fluxo.execucao.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IniciarExecucaoDTO(
        @NotNull(message = "Animal e obrigatorio") Long animalId,
        @NotNull(message = "Fluxo e obrigatorio") Long fluxoId,
        @NotNull(message = "Data de inicio e obrigatoria") LocalDate dataInicio
) {}
