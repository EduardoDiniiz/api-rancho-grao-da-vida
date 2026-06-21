package com.rancho.api.baia.dto;

import com.rancho.api.baia.BaiaStatus;

public record BaiaResponseDTO(
        Long id,
        String identificacao,
        String localizacao,
        Integer capacidade,
        BaiaStatus status,
        String observacao,
        String animalAtual
) {}
