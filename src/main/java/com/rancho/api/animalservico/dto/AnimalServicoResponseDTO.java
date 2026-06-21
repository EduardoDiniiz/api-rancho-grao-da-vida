package com.rancho.api.animalservico.dto;

import com.rancho.api.animalservico.AnimalServicoStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalServicoResponseDTO(
        Long id,
        Long animalId,
        String animalNome,
        Long servicoId,
        String servicoNome,
        BigDecimal valor,
        LocalDate dataInicio,
        LocalDate proximoVencimento,
        Integer recorrenciaDias,
        String descricao,
        AnimalServicoStatus status
) {}
