package com.npl.na_ponta_do_lapis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContaFinanceira c WHERE c.id = :id")
    Optional<ContaFinanceira> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT c FROM ContaFinanceira c WHERE c.usuario.email = :email")
    List<ContaFinanceira> buscarContaFinanceiraUsuarioLogado(String email);

    @Query("SELECT c FROM ContaFinanceira c WHERE c.usuario.familia.id = :familiaId")
    List<ContaFinanceira> findByFamilia(Long familiaId);

    java.util.Optional<ContaFinanceira> findByPluggyAccountIdAndUsuarioId(String pluggyAccountId, Long usuarioId);

    int countByUsuarioIdAndSincronizadaTrue(Long usuarioId);

    List<ContaFinanceira> findAllByUsuarioIdAndSincronizadaTrue(Long usuarioId);
}
