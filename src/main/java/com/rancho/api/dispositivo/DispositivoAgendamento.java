package com.rancho.api.dispositivo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** Agendamento diario de liga/desliga de um dispositivo Tuya. */
@Entity
@Table(name = "dispositivo_agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"deviceId"})
public class DispositivoAgendamento {

    @Id
    @Column(name = "device_id", length = 64)
    private String deviceId;

    /** Hora de ligar todos os dias (null = nao liga automaticamente). */
    @Column(name = "hora_ligar")
    private LocalTime horaLigar;

    /** Hora de desligar todos os dias (null = nao desliga automaticamente). */
    @Column(name = "hora_desligar")
    private LocalTime horaDesligar;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
