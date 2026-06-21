package com.rancho.api.animal.dto;

import com.rancho.api.animal.AnimalStatus;
import com.rancho.api.animal.Esporte;
import com.rancho.api.animal.Sexo;

import java.time.LocalDate;

public record AnimalResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        String nome,
        LocalDate dataNascimento,
        Sexo sexo,
        Esporte esporte,
        String registro,
        String enfermidades,
        String observacoes,
        AnimalStatus status
) {}
