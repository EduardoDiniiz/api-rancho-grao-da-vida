package com.rancho.api.fluxo;

import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.fluxo.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FluxoService {

    private final FluxoRepository fluxoRepository;
    private final FluxoPontoRepository pontoRepository;
    private final FluxoArestaRepository arestaRepository;

    @Transactional(readOnly = true)
    public List<FluxoResumoDTO> listar(String search) {
        String param = (search != null && !search.isBlank()) ? "%" + search.toLowerCase() + "%" : null;
        return fluxoRepository.search(param).stream()
                .map(f -> new FluxoResumoDTO(
                        f.getId(), f.getNome(), f.getDescricao(), f.isActive(),
                        pontoRepository.countByFluxoId(f.getId())))
                .toList();
    }

    @Transactional
    public FluxoResumoDTO criar(FluxoRequestDTO dto) {
        Fluxo fluxo = Fluxo.builder().nome(dto.nome()).descricao(dto.descricao()).build();
        fluxo = fluxoRepository.save(fluxo);
        return new FluxoResumoDTO(fluxo.getId(), fluxo.getNome(), fluxo.getDescricao(), fluxo.isActive(), 0);
    }

    @Transactional
    public FluxoResumoDTO atualizarMeta(Long id, FluxoRequestDTO dto) {
        Fluxo fluxo = getFluxo(id);
        fluxo.setNome(dto.nome());
        fluxo.setDescricao(dto.descricao());
        fluxoRepository.save(fluxo);
        return new FluxoResumoDTO(fluxo.getId(), fluxo.getNome(), fluxo.getDescricao(), fluxo.isActive(),
                pontoRepository.countByFluxoId(id));
    }

    @Transactional(readOnly = true)
    public FluxoGrafoResponseDTO buscarGrafo(Long id) {
        Fluxo fluxo = getFluxo(id);
        List<PontoDTO> pontos = pontoRepository.findByFluxoId(id).stream()
                .map(p -> new PontoDTO(
                        p.getNodeKey(), p.getTipo(), p.getTitulo(), p.getDia(),
                        p.getProduto(), p.getDose(), p.getDescricao(), p.isInicial(),
                        p.getPosX(), p.getPosY()))
                .toList();
        List<ArestaDTO> arestas = arestaRepository.findByFluxoId(id).stream()
                .map(a -> new ArestaDTO(a.getOrigem().getNodeKey(), a.getDestino().getNodeKey(), a.getCondicao()))
                .toList();
        return new FluxoGrafoResponseDTO(fluxo.getId(), fluxo.getNome(), fluxo.getDescricao(), fluxo.isActive(), pontos, arestas);
    }

    /** Substitui todo o grafo do fluxo (apaga pontos/arestas e recria a partir do payload). */
    @Transactional
    public FluxoGrafoResponseDTO salvarGrafo(Long id, GrafoRequestDTO dto) {
        Fluxo fluxo = getFluxo(id);

        arestaRepository.deleteByFluxoId(id);
        pontoRepository.deleteByFluxoId(id);
        arestaRepository.flush();
        pontoRepository.flush();

        Map<String, FluxoPonto> porChave = new HashMap<>();
        if (dto.pontos() != null) {
            for (PontoDTO p : dto.pontos()) {
                FluxoPonto ponto = FluxoPonto.builder()
                        .fluxo(fluxo)
                        .nodeKey(p.nodeKey())
                        .tipo(p.tipo())
                        .titulo(p.titulo())
                        .dia(p.dia())
                        .produto(p.produto())
                        .dose(p.dose())
                        .descricao(p.descricao())
                        .inicial(p.inicial() != null && p.inicial())
                        .posX(p.posX())
                        .posY(p.posY())
                        .build();
                porChave.put(p.nodeKey(), pontoRepository.save(ponto));
            }
        }
        if (dto.arestas() != null) {
            for (ArestaDTO a : dto.arestas()) {
                FluxoPonto origem = porChave.get(a.origemKey());
                FluxoPonto destino = porChave.get(a.destinoKey());
                if (origem == null || destino == null) continue;
                arestaRepository.save(FluxoAresta.builder()
                        .fluxo(fluxo).origem(origem).destino(destino).condicao(a.condicao()).build());
            }
        }
        return buscarGrafo(id);
    }

    @Transactional
    public void excluir(Long id) {
        fluxoRepository.delete(getFluxo(id));
    }

    public Fluxo getFluxo(Long id) {
        return fluxoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fluxo", id));
    }
}
