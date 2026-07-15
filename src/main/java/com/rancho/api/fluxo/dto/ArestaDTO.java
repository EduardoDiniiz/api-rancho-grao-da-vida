package com.rancho.api.fluxo.dto;

import jakarta.validation.constraints.NotBlank;

/** Ligacao entre dois pontos, referenciando-os pelo nodeKey. */
public record ArestaDTO(
        @NotBlank String origemKey,
        @NotBlank String destinoKey,
        String condicao
) {}
