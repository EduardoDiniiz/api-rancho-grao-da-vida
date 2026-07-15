package com.rancho.api.fluxo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Criacao/edicao dos metadados do fluxo (nome e descricao). */
public record FluxoRequestDTO(
        @NotBlank(message = "Nome do fluxo e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String nome,

        String descricao
) {}
