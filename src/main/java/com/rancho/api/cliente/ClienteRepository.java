package com.rancho.api.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("""
            SELECT c FROM Cliente c
            WHERE c.active = true
              AND (:search IS NULL
                OR LOWER(c.nome) LIKE :search
                OR (:digits IS NOT NULL AND c.cpfCnpj LIKE :digits)
                OR (:digits IS NOT NULL AND c.telefone LIKE :digits))
            """)
    Page<Cliente> search(@Param("search") String search, @Param("digits") String digits, Pageable pageable);

    long countByActiveTrue();
}
