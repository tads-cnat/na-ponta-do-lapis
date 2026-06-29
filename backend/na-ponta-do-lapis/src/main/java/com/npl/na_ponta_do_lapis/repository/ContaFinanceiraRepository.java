package com.npl.na_ponta_do_lapis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, Long> {

    @Query("SELECT c FROM ContaFinanceira c WHERE c.usuario.email = :email")
    List<ContaFinanceira> buscarContaFinanceiraUsuarioLogado(String email);

    @Query("SELECT c FROM ContaFinanceira c WHERE c.usuario.familia.id = :familiaId")
    List<ContaFinanceira> findByFamilia(Long familiaId);
}
