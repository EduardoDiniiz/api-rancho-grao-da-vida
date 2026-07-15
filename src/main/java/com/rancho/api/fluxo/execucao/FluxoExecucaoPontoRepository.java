package com.rancho.api.fluxo.execucao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FluxoExecucaoPontoRepository extends JpaRepository<FluxoExecucaoPonto, Long> {

    List<FluxoExecucaoPonto> findByExecucaoIdOrderByDataPrevistaAscIdAsc(Long execucaoId);

    long countByExecucaoId(Long execucaoId);

    long countByExecucaoIdAndStatus(Long execucaoId, PontoExecStatus status);

    long countByExecucaoIdAndStatusNot(Long execucaoId, PontoExecStatus status);
}
