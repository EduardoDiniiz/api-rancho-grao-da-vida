package com.rancho.api.fluxo.dto;

public record FluxoResumoDTO(
        Long id,
        String nome,
        String descricao,
        boolean active,
        long totalPontos
) {}
