package com.rancho.api.pagamento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByAsaasPaymentId(String asaasPaymentId);

    @Query("""
            SELECT p FROM Pagamento p
              LEFT JOIN p.animal a
              LEFT JOIN a.cliente c
            WHERE (:status IS NULL OR p.status = :status)
              AND (:animalId IS NULL OR a.id = :animalId)
              AND (:clienteId IS NULL OR c.id = :clienteId)
              AND (:inicio IS NULL OR p.vencimento >= :inicio)
              AND (:fim IS NULL OR p.vencimento <= :fim)
            ORDER BY p.vencimento ASC
            """)
    Page<Pagamento> search(@Param("status") PagamentoStatus status,
                          @Param("animalId") Long animalId,
                          @Param("clienteId") Long clienteId,
                          @Param("inicio") LocalDate inicio,
                          @Param("fim") LocalDate fim,
                          Pageable pageable);

    List<Pagamento> findByStatusAndVencimentoBefore(PagamentoStatus status, LocalDate data);

    boolean existsByAnimalServicoId(Long animalServicoId);

    @Query("""
            SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p
            WHERE p.status = :status
            """)
    BigDecimal sumByStatus(@Param("status") PagamentoStatus status);

    @Query("""
            SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p
            WHERE p.status = :status
              AND p.dataPagamento BETWEEN :inicio AND :fim
            """)
    BigDecimal sumPagoNoPeriodo(@Param("status") PagamentoStatus status,
                                @Param("inicio") LocalDate inicio,
                                @Param("fim") LocalDate fim);

    long countByStatus(PagamentoStatus status);
}
