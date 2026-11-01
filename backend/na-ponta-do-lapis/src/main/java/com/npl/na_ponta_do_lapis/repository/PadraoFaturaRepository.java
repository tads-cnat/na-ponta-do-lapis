package com.npl.na_ponta_do_lapis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;

public interface PadraoFaturaRepository extends JpaRepository<PadraoFatura, Long> {

    List<PadraoFatura> findByAtivoTrueOrderByVersaoDesc();

    List<PadraoFatura> findByEmissorAndAtivoTrueOrderByVersaoDesc(String emissor);

    Optional<PadraoFatura> findFirstByEmissorAndAtivoTrueOrderByVersaoDesc(String emissor);

    boolean existsByEmissorAndAtivoTrue(String emissor);
}
