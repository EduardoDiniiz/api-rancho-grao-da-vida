package com.rancho.api.fluxo.execucao;

import jakarta.persistence.*;
import lombok.*;

/** Snapshot de uma aresta do fluxo dentro de uma execucao (para desenhar o caminho). */
@Entity
@Table(name = "fluxo_execucao_arestas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class FluxoExecucaoAresta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execucao_id", nullable = false)
    private FluxoExecucao execucao;

    @Column(name = "origem_key", nullable = false, length = 50)
    private String origemKey;

    @Column(name = "destino_key", nullable = false, length = 50)
    private String destinoKey;

    @Column(length = 255)
    private String condicao;
}
