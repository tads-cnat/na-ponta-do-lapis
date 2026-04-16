package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoCategoriaRepository extends JpaRepository<TipoCategoria, Long> {
}
