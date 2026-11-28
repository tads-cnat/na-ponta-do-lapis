package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.ConfiguracaoIaProvedor;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracaoIaProvedorRepository extends JpaRepository<ConfiguracaoIaProvedor, ProvedorIa> {
}

