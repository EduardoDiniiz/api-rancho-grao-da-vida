package com.rancho.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "Informe o usuario ou email")
        String login,

        @NotBlank(message = "Senha e obrigatoria")
        String password
) {}
