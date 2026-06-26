package com.rancho.api.dispositivo.dto;

import jakarta.validation.constraints.Size;

/** Novo apelido local do dispositivo. Em branco remove o apelido (volta ao nome da Tuya). */
public record RenomearRequestDTO(
        @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
        String nome
) {}
