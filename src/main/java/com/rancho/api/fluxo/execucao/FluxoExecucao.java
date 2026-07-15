package com.rancho.api.fluxo.execucao;

import com.rancho.api.animal.Animal;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Instancia de um fluxo de tratamento aplicado a um animal (com snapshot dos pontos). */
@Entity
@Table(name = "fluxo_execucoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class FluxoExecucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    /** Id do template de origem (pode ficar null se o fluxo for excluido). */
    @Column(name = "fluxo_id")
    private Long fluxoId;

    @Column(name = "fluxo_nome", nullable = false)
    private String fluxoNome;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExecucaoStatus status = ExecucaoStatus.EM_ANDAMENTO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
