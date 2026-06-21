package com.rancho.api.animal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    @Query("""
            SELECT a FROM Animal a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:clienteId IS NULL OR a.cliente.id = :clienteId)
              AND (:search IS NULL
                OR LOWER(a.nome) LIKE :search
                OR LOWER(a.registro) LIKE :search)
            """)
    Page<Animal> search(@Param("search") String search,
                        @Param("status") AnimalStatus status,
                        @Param("clienteId") Long clienteId,
                        Pageable pageable);

    long countByClienteId(Long clienteId);

    long countByClienteIdAndStatus(Long clienteId, AnimalStatus status);

    long countByStatus(AnimalStatus status);
}
