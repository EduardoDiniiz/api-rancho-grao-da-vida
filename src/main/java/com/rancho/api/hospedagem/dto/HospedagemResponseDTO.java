package com.rancho.api.hospedagem.dto;

import com.rancho.api.hospedagem.HospedagemStatus;

import java.time.LocalDate;

public record HospedagemResponseDTO(
        Long id,
        Long animalId,
        String animalNome,
        Long clienteId,
        String clienteNome,
        Long baiaId,
        String baiaIdentificacao,
        LocalDate dataEntrada,
        LocalDate dataSaida,
        HospedagemStatus status
) {}
