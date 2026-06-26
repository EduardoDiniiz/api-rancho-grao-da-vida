package com.rancho.api.dispositivo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

/**
 * Um interruptor/dispositivo WiFi exposto pela Tuya Cloud.
 *
 * @param ligado    estado atual do interruptor (null se nao foi possivel determinar)
 * @param switchCode codigo do "data point" usado para ligar/desligar (ex: switch_1)
 * @param horaLigar     horario do agendamento diario para ligar (null = sem)
 * @param horaDesligar  horario do agendamento diario para desligar (null = sem)
 * @param agendamentoAtivo se o agendamento esta habilitado
 */
public record DispositivoResponseDTO(
        String id,
        String nome,
        String categoria,
        Boolean online,
        Boolean ligado,
        String switchCode,
        @JsonFormat(pattern = "HH:mm") LocalTime horaLigar,
        @JsonFormat(pattern = "HH:mm") LocalTime horaDesligar,
        Boolean agendamentoAtivo
) {}
