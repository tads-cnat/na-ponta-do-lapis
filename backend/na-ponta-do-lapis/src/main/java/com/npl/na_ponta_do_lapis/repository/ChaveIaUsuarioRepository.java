package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.ChaveIaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChaveIaUsuarioRepository extends JpaRepository<ChaveIaUsuario, Long> {

    List<ChaveIaUsuario> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    Optional<ChaveIaUsuario> findByIdAndUsuarioId(Long id, Long usuarioId);

    Optional<ChaveIaUsuario> findByUsuarioIdAndPadraoTrue(Long usuarioId);
}

