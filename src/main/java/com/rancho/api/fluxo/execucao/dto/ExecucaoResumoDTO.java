package com.rancho.api.fluxo.execucao.dto;

import com.rancho.api.fluxo.execucao.ExecucaoStatus;

import java.time.LocalDate;

public record ExecucaoResumoDTO(
        Long id,
        Long animalId,
        String fluxoNome,
        LocalDate dataInicio,
        ExecucaoStatus status,
        long totalPontos,
        long concluidos
) {}
