package com.rancho.api.fluxo.dto;

import java.util.List;

/** Fluxo completo com seu grafo (pontos + arestas), usado pelo editor. */
public record FluxoGrafoResponseDTO(
        Long id,
        String nome,
        String descricao,
        boolean active,
        List<PontoDTO> pontos,
        List<ArestaDTO> arestas
) {}
