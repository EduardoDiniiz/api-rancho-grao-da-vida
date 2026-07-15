package com.rancho.api.fluxo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FluxoPontoRepository extends JpaRepository<FluxoPonto, Long> {

    List<FluxoPonto> findByFluxoId(Long fluxoId);

    void deleteByFluxoId(Long fluxoId);

    long countByFluxoId(Long fluxoId);
}
