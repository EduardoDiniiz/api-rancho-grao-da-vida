package com.rancho.api.exame;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExameRepository extends JpaRepository<Exame, Long> {

    List<Exame> findByAnimalIdOrderByDataDesc(Long animalId);
}
