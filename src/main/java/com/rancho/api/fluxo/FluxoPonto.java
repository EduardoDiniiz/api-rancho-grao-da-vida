package com.rancho.api.fluxo;

import jakarta.persistence.*;
import lombok.*;

/** Um ponto (no) do grafo de tratamento. */
@Entity
@Table(name = "fluxo_pontos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class FluxoPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fluxo_id", nullable = false)
    private Fluxo fluxo;

    /** Id do no usado pelo editor visual (React Flow); referenciado pelas arestas no payload. */
    @Column(name = "node_key", nullable = false, length = 50)
    private String nodeKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PontoTipo tipo;

    @Column(nullable = false)
    private String titulo;

    /** Offset em dias a partir do inicio do fluxo. */
    private Integer dia;

    private String produto;

    @Column(length = 100)
    private String dose;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private boolean inicial = false;

    @Column(name = "pos_x", nullable = false)
    @Builder.Default
    private double posX = 0;

    @Column(name = "pos_y", nullable = false)
    @Builder.Default
    private double posY = 0;
}
