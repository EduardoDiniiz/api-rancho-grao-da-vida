package com.rancho.api.dispositivo.dto;

import jakarta.validation.constraints.NotNull;

/** Comando de ligar (true) / desligar (false) um dispositivo. */
public record ComandoRequestDTO(
        @NotNull Boolean ligado
) {}
