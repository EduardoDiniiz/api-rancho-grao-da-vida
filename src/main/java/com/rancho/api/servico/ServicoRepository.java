package com.rancho.api.servico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Optional<Servico> findFirstByNomeIgnoreCase(String nome);

    @Query("""
            SELECT s FROM Servico s
            WHERE (:apenasAtivos = false OR s.active = true)
              AND (:search IS NULL OR LOWER(s.nome) LIKE :search)
            """)
    Page<Servico> search(@Param("search") String search,
                        @Param("apenasAtivos") boolean apenasAtivos,
                        Pageable pageable);
}
