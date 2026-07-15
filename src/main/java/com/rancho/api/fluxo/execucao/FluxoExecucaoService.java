package com.rancho.api.fluxo.execucao;

import com.rancho.api.animal.Animal;
import com.rancho.api.animal.AnimalService;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.exame.ExameService;
import com.rancho.api.exame.dto.ExameRequestDTO;
import com.rancho.api.fluxo.FluxoService;
import com.rancho.api.fluxo.dto.FluxoGrafoResponseDTO;
import com.rancho.api.fluxo.execucao.dto.*;
import com.rancho.api.vacina.VacinaService;
import com.rancho.api.vacina.dto.VacinaRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FluxoExecucaoService {

    private final FluxoExecucaoRepository execucaoRepository;
    private final FluxoExecucaoPontoRepository pontoRepository;
    private final FluxoExecucaoArestaRepository arestaRepository;
    private final FluxoService fluxoService;
    private final AnimalService animalService;
    private final ExameService exameService;
    private final VacinaService vacinaService;

    /** Inicia um fluxo para um animal: cria a execucao e fotografa (snapshot) pontos e arestas. */
    @Transactional
    public ExecucaoResumoDTO iniciar(IniciarExecucaoDTO dto) {
        Animal animal = animalService.getAnimal(dto.animalId());
        FluxoGrafoResponseDTO grafo = fluxoService.buscarGrafo(dto.fluxoId());

        FluxoExecucao execucao = execucaoRepository.save(FluxoExecucao.builder()
                .animal(animal)
                .fluxoId(grafo.id())
                .fluxoNome(grafo.nome())
                .dataInicio(dto.dataInicio())
                .status(ExecucaoStatus.EM_ANDAMENTO)
                .build());

        grafo.pontos().forEach(p -> {
            LocalDate prevista = p.dia() == null ? dto.dataInicio() : dto.dataInicio().plusDays(p.dia());
            pontoRepository.save(FluxoExecucaoPonto.builder()
                    .execucao(execucao)
                    .nodeKey(p.nodeKey())
                    .tipo(p.tipo())
                    .titulo(p.titulo())
                    .dia(p.dia())
                    .produto(p.produto())
                    .dose(p.dose())
                    .descricao(p.descricao())
                    .dataPrevista(prevista)
                    .status(PontoExecStatus.PENDENTE)
                    .build());
        });

        grafo.arestas().forEach(a -> arestaRepository.save(FluxoExecucaoAresta.builder()
                .execucao(execucao)
                .origemKey(a.origemKey())
                .destinoKey(a.destinoKey())
                .condicao(a.condicao())
                .build()));

        return resumo(execucao);
    }

    @Transactional(readOnly = true)
    public List<ExecucaoResumoDTO> listarPorAnimal(Long animalId) {
        return execucaoRepository.findByAnimalIdOrderByDataInicioDesc(animalId)
                .stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public ExecucaoDetalheDTO buscar(Long id) {
        FluxoExecucao ex = getExecucao(id);
        List<ExecPontoDTO> pontos = pontoRepository.findByExecucaoIdOrderByDataPrevistaAscIdAsc(id)
                .stream().map(this::toPontoDTO).toList();
        List<ExecArestaDTO> arestas = arestaRepository.findByExecucaoId(id).stream()
                .map(a -> new ExecArestaDTO(a.getOrigemKey(), a.getDestinoKey(), a.getCondicao()))
                .toList();
        return new ExecucaoDetalheDTO(ex.getId(), ex.getAnimal().getId(), ex.getAnimal().getNome(),
                ex.getFluxoNome(), ex.getDataInicio(), ex.getStatus(), pontos, arestas);
    }

    /** Conclui um ponto. Se for EXAME/VACINA, gera o registro no modulo correspondente. */
    @Transactional
    public ExecPontoDTO concluirPonto(Long pontoId, String observacao) {
        FluxoExecucaoPonto ponto = getPonto(pontoId);
        ponto.setStatus(PontoExecStatus.CONCLUIDO);
        ponto.setDataConclusao(LocalDate.now());
        ponto.setObservacao(observacao);
        pontoRepository.save(ponto);

        Long animalId = ponto.getExecucao().getAnimal().getId();
        switch (ponto.getTipo()) {
            case EXAME -> exameService.create(new ExameRequestDTO(
                    animalId, ponto.getTitulo(), LocalDate.now(), null, null, observacao));
            case VACINA -> vacinaService.create(new VacinaRequestDTO(
                    animalId, ponto.getTitulo(), LocalDate.now(), null, observacao));
            default -> { /* MEDICAMENTO / ALIMENTACAO: sem integracao */ }
        }

        atualizarStatusExecucao(ponto.getExecucao());
        return toPontoDTO(ponto);
    }

    @Transactional
    public ExecPontoDTO pularPonto(Long pontoId) {
        FluxoExecucaoPonto ponto = getPonto(pontoId);
        ponto.setStatus(PontoExecStatus.PULADO);
        ponto.setDataConclusao(null);
        ponto.setObservacao(null);
        pontoRepository.save(ponto);
        atualizarStatusExecucao(ponto.getExecucao());
        return toPontoDTO(ponto);
    }

    @Transactional
    public ExecPontoDTO reabrirPonto(Long pontoId) {
        FluxoExecucaoPonto ponto = getPonto(pontoId);
        ponto.setStatus(PontoExecStatus.PENDENTE);
        ponto.setDataConclusao(null);
        pontoRepository.save(ponto);
        FluxoExecucao ex = ponto.getExecucao();
        if (ex.getStatus() == ExecucaoStatus.CONCLUIDO) {
            ex.setStatus(ExecucaoStatus.EM_ANDAMENTO);
            execucaoRepository.save(ex);
        }
        return toPontoDTO(ponto);
    }

    @Transactional
    public void cancelar(Long id) {
        FluxoExecucao ex = getExecucao(id);
        ex.setStatus(ExecucaoStatus.CANCELADO);
        execucaoRepository.save(ex);
    }

    // ---- helpers ----

    /** Conclui a execucao automaticamente quando nao ha mais pontos pendentes. */
    private void atualizarStatusExecucao(FluxoExecucao ex) {
        if (ex.getStatus() == ExecucaoStatus.CANCELADO) return;
        long pendentes = pontoRepository.countByExecucaoIdAndStatus(ex.getId(), PontoExecStatus.PENDENTE);
        ExecucaoStatus novo = pendentes == 0 ? ExecucaoStatus.CONCLUIDO : ExecucaoStatus.EM_ANDAMENTO;
        if (ex.getStatus() != novo) {
            ex.setStatus(novo);
            execucaoRepository.save(ex);
        }
    }

    private ExecucaoResumoDTO resumo(FluxoExecucao ex) {
        long total = pontoRepository.countByExecucaoId(ex.getId());
        long concluidos = pontoRepository.countByExecucaoIdAndStatus(ex.getId(), PontoExecStatus.CONCLUIDO);
        return new ExecucaoResumoDTO(ex.getId(), ex.getAnimal().getId(), ex.getFluxoNome(),
                ex.getDataInicio(), ex.getStatus(), total, concluidos);
    }

    private ExecPontoDTO toPontoDTO(FluxoExecucaoPonto p) {
        return new ExecPontoDTO(p.getId(), p.getNodeKey(), p.getTipo(), p.getTitulo(), p.getDia(),
                p.getProduto(), p.getDose(), p.getDescricao(), p.getDataPrevista(),
                p.getStatus(), p.getDataConclusao(), p.getObservacao());
    }

    private FluxoExecucao getExecucao(Long id) {
        return execucaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Execucao de fluxo", id));
    }

    private FluxoExecucaoPonto getPonto(Long id) {
        return pontoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto da execucao", id));
    }
}
