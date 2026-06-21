package com.rancho.api.animalservico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalServicoRepository extends JpaRepository<AnimalServico, Long> {

    List<AnimalServico> findByAnimalIdOrderByCreatedAtDesc(Long animalId);

    boolean existsByServicoId(Long servicoId);
}
