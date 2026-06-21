package com.rancho.api.user.dto;

import com.rancho.api.user.Role;
import jakarta.validation.constraints.*;

public record UserCreateDTO(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String name,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "Login e obrigatorio")
        @Size(min = 3, max = 50, message = "Login deve ter entre 3 e 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Login deve conter apenas letras, numeros, ponto, hifen ou underline")
        String login,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 6, message = "Senha deve ter no minimo 6 caracteres")
        String password,

        @NotNull(message = "Perfil e obrigatorio")
        Role role
) {}
