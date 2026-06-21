package com.rancho.api.user.dto;

import com.rancho.api.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String name,

        @Email(message = "Email invalido")
        String email,

        Role role,

        Boolean active
) {}
