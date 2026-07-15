package com.rancho.api.fluxo.dto;

import jakarta.validation.Valid;

import java.util.List;

/** Payload para salvar o grafo inteiro (substitui pontos e arestas do fluxo). */
public record GrafoRequestDTO(
        @Valid List<PontoDTO> pontos,
        @Valid List<ArestaDTO> arestas
) {}
