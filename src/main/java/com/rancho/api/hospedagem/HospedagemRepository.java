package com.rancho.api.hospedagem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HospedagemRepository extends JpaRepository<Hospedagem, Long> {

    Optional<Hospedagem> findFirstByBaiaIdAndStatus(Long baiaId, HospedagemStatus status);

    boolean existsByBaiaIdAndStatus(Long baiaId, HospedagemStatus status);

    boolean existsByAnimalIdAndStatus(Long animalId, HospedagemStatus status);

    @Query("""
            SELECT h FROM Hospedagem h
            WHERE (:status IS NULL OR h.status = :status)
              AND (:animalId IS NULL OR h.animal.id = :animalId)
              AND (:baiaId IS NULL OR h.baia.id = :baiaId)
            ORDER BY h.dataEntrada DESC
            """)
    Page<Hospedagem> search(@Param("status") HospedagemStatus status,
                           @Param("animalId") Long animalId,
                           @Param("baiaId") Long baiaId,
                           Pageable pageable);

    long countByStatus(HospedagemStatus status);
}
