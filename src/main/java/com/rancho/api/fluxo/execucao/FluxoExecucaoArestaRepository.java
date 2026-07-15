package com.rancho.api.fluxo.execucao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FluxoExecucaoArestaRepository extends JpaRepository<FluxoExecucaoAresta, Long> {

    List<FluxoExecucaoAresta> findByExecucaoId(Long execucaoId);
}
