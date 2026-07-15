package com.rancho.api.fluxo.execucao;

import com.rancho.api.fluxo.PontoTipo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** Snapshot de um ponto do fluxo dentro de uma execucao, com estado de conclusao. */
@Entity
@Table(name = "fluxo_execucao_pontos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class FluxoExecucaoPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execucao_id", nullable = false)
    private FluxoExecucao execucao;

    @Column(name = "node_key", nullable = false, length = 50)
    private String nodeKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PontoTipo tipo;

    @Column(nullable = false)
    private String titulo;

    private Integer dia;

    private String produto;

    @Column(length = 100)
    private String dose;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PontoExecStatus status = PontoExecStatus.PENDENTE;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}
