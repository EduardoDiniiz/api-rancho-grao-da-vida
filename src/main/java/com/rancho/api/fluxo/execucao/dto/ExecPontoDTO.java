package com.rancho.api.fluxo.execucao.dto;

import com.rancho.api.fluxo.PontoTipo;
import com.rancho.api.fluxo.execucao.PontoExecStatus;

import java.time.LocalDate;

public record ExecPontoDTO(
        Long id,
        String nodeKey,
        PontoTipo tipo,
        String titulo,
        Integer dia,
        String produto,
        String dose,
        String descricao,
        LocalDate dataPrevista,
        PontoExecStatus status,
        LocalDate dataConclusao,
        String observacao
) {}
