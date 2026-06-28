package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoCategoriaRepository extends JpaRepository<TipoCategoria, Long> {

    Optional<TipoCategoria> findByNome(String nome);

}
