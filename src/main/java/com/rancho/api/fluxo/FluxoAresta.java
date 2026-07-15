package com.rancho.api.fluxo;

import jakarta.persistence.*;
import lombok.*;

/** Ligacao direcionada entre dois pontos do fluxo. A condicao rotula ramificacoes. */
@Entity
@Table(name = "fluxo_arestas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class FluxoAresta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fluxo_id", nullable = false)
    private Fluxo fluxo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origem_id", nullable = false)
    private FluxoPonto origem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private FluxoPonto destino;

    @Column(length = 255)
    private String condicao;
}
