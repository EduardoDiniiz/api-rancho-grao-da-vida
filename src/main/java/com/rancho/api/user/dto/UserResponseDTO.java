package com.rancho.api.user.dto;

import com.rancho.api.user.Role;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String login,
        Role role,
        Boolean active,
        LocalDateTime createdAt
) {}
