package com.npl.na_ponta_do_lapis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;

public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, Long> {
}
