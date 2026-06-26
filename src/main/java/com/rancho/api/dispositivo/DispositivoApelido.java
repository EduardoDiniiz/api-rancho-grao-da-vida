package com.rancho.api.dispositivo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** Apelido local (nome customizado) para um dispositivo Tuya, identificado pelo id do device. */
@Entity
@Table(name = "dispositivo_apelidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"deviceId"})
public class DispositivoApelido {

    @Id
    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(nullable = false, length = 255)
    private String apelido;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
