package com.rancho.api.cliente.dto;

import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        String endereco,
        String observacoes,
        Boolean active,
        Long totalAnimais,
        Long usuarioId,
        String usuarioLogin,
        LocalDateTime createdAt
) {}
