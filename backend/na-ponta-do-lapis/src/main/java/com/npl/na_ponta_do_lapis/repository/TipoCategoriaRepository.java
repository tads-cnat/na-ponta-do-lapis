package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoCategoriaRepository extends JpaRepository<TipoCategoria, Long> {

    @Query("SELECT tc FROM TipoCategoria tc WHERE tc.usuario.email = :email")
    List<TipoCategoria> buscarTipoCategoriaPorEmail(String email);
}
