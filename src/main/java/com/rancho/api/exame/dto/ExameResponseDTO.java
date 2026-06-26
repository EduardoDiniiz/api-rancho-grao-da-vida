package com.rancho.api.exame.dto;

import java.time.LocalDate;

public record ExameResponseDTO(
        Long id,
        Long animalId,
        String animalNome,
        String nome,
        LocalDate data,
        String resultado,
        String veterinario,
        String observacao
) {}
