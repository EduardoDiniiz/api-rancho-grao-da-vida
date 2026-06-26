package com.rancho.api.dispositivo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

/**
 * Configuracao do agendamento diario. Horas em formato "HH:mm" (null = sem disparo).
 */
public record AgendamentoRequestDTO(
        @JsonFormat(pattern = "HH:mm") LocalTime horaLigar,
        @JsonFormat(pattern = "HH:mm") LocalTime horaDesligar,
        Boolean ativo
) {}
