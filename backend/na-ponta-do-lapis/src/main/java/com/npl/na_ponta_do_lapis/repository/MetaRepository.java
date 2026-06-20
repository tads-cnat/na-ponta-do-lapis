package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaRepository extends JpaRepository<Meta, Long> {

    List<Meta> findByUsuarioId(Long usuarioId);
}