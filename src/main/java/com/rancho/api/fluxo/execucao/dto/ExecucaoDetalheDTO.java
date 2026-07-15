package com.rancho.api.fluxo.execucao.dto;

import com.rancho.api.fluxo.execucao.ExecucaoStatus;

import java.time.LocalDate;
import java.util.List;

public record ExecucaoDetalheDTO(
        Long id,
        Long animalId,
        String animalNome,
        String fluxoNome,
        LocalDate dataInicio,
        ExecucaoStatus status,
        List<ExecPontoDTO> pontos,
        List<ExecArestaDTO> arestas
) {}
