package com.rancho.api.baia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BaiaRepository extends JpaRepository<Baia, Long> {

    boolean existsByIdentificacao(String identificacao);

    @Query("""
            SELECT b FROM Baia b
            WHERE (:status IS NULL OR b.status = :status)
              AND (:search IS NULL
                OR LOWER(b.identificacao) LIKE :search
                OR LOWER(b.localizacao) LIKE :search)
            """)
    Page<Baia> search(@Param("search") String search,
                      @Param("status") BaiaStatus status,
                      Pageable pageable);

    long countByStatus(BaiaStatus status);
}
