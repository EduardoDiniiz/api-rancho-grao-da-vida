package com.rancho.api.vacina.dto;

import java.time.LocalDate;

public record VacinaResponseDTO(
        Long id,
        Long animalId,
        String animalNome,
        String nome,
        LocalDate dataAplicacao,
        LocalDate dataVencimento,
        String observacao,
        String situacao
) {}
