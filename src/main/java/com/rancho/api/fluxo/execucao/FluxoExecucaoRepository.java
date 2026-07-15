package com.rancho.api.fluxo.execucao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FluxoExecucaoRepository extends JpaRepository<FluxoExecucao, Long> {

    List<FluxoExecucao> findByAnimalIdOrderByDataInicioDesc(Long animalId);
}
