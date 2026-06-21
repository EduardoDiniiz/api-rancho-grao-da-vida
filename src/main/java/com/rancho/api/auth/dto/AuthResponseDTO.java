package com.rancho.api.auth.dto;

import com.rancho.api.user.Role;

public record AuthResponseDTO(
        String token,
        String type,
        long expiresIn,
        Long userId,
        String name,
        Role role
) {
    public AuthResponseDTO(String token, long expiresIn, Long userId, String name, Role role) {
        this(token, "Bearer", expiresIn, userId, name, role);
    }
}
