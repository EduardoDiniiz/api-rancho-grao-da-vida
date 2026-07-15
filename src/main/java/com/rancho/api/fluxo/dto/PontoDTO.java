package com.rancho.api.fluxo.dto;

import com.rancho.api.fluxo.PontoTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Um ponto do grafo. nodeKey e o id usado pelo editor e referenciado pelas arestas. */
public record PontoDTO(
        @NotBlank String nodeKey,
        @NotNull PontoTipo tipo,
        @NotBlank String titulo,
        Integer dia,
        String produto,
        String dose,
        String descricao,
        Boolean inicial,
        double posX,
        double posY
) {}
