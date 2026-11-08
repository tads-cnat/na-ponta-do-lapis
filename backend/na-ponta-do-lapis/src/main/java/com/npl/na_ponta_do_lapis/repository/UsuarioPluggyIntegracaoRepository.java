package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.UsuarioPluggyIntegracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioPluggyIntegracaoRepository extends JpaRepository<UsuarioPluggyIntegracao, Long> {

    Optional<UsuarioPluggyIntegracao> findByUsuarioId(Long usuarioId);

    List<UsuarioPluggyIntegracao> findAllByClientIdCriptografadoIsNotNull();
}

