package com.rancho.api.hospedagem.dto;

import com.rancho.api.hospedagem.HospedagemStatus;

import java.math.BigDecimal;
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
        BigDecimal valorMensal,
        LocalDate proximoVencimento,
        HospedagemStatus status
) {}
