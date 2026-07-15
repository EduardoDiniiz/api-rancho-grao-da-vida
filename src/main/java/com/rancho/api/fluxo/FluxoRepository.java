package com.rancho.api.fluxo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FluxoRepository extends JpaRepository<Fluxo, Long> {

    @Query("""
            SELECT f FROM Fluxo f
            WHERE (:search IS NULL OR LOWER(f.nome) LIKE :search)
            ORDER BY f.nome ASC
            """)
    List<Fluxo> search(@Param("search") String search);
}
