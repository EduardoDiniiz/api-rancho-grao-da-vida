package com.rancho.api.fluxo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FluxoArestaRepository extends JpaRepository<FluxoAresta, Long> {

    List<FluxoAresta> findByFluxoId(Long fluxoId);

    void deleteByFluxoId(Long fluxoId);
}
