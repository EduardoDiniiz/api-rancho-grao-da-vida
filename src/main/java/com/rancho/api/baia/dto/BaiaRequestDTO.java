package com.rancho.api.baia.dto;

import com.rancho.api.baia.BaiaStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BaiaRequestDTO(
        @NotBlank(message = "Identificacao e obrigatoria")
        @Size(max = 50, message = "Identificacao deve ter no maximo 50 caracteres")
        String identificacao,

        @Size(max = 255, message = "Localizacao deve ter no maximo 255 caracteres")
        String localizacao,

        @NotNull(message = "Capacidade e obrigatoria")
        @Min(value = 1, message = "Capacidade minima e 1")
        Integer capacidade,

        BaiaStatus status,

        String observacao
) {}
