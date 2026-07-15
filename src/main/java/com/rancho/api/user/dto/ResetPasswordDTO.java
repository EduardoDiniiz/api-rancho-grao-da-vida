package com.rancho.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Redefinicao de senha por um administrador (nao exige a senha atual).
 * Usada na tela de edicao de usuario.
 */
public record ResetPasswordDTO(
        @NotBlank(message = "Nova senha e obrigatoria")
        @Size(min = 6, message = "Nova senha deve ter no minimo 6 caracteres")
        String newPassword
) {}
