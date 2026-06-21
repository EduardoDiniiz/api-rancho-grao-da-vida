package com.rancho.api.vacina;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VacinaRepository extends JpaRepository<Vacina, Long> {

    List<Vacina> findByAnimalIdOrderByDataAplicacaoDesc(Long animalId);

    @Query("""
            SELECT v FROM Vacina v
            WHERE v.dataVencimento IS NOT NULL
              AND v.dataVencimento BETWEEN :inicio AND :fim
            ORDER BY v.dataVencimento ASC
            """)
    List<Vacina> findVencendoEntre(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            SELECT v FROM Vacina v
            WHERE v.dataVencimento IS NOT NULL
              AND v.dataVencimento < :hoje
            ORDER BY v.dataVencimento ASC
            """)
    List<Vacina> findVencidas(@Param("hoje") LocalDate hoje);
}
