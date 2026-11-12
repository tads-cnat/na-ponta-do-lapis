package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.UsuarioConquista;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioConquistaRepository extends JpaRepository<UsuarioConquista, Long> {

    List<UsuarioConquista> findByUsuarioIdOrderByDataDesbloqueioDesc(Long usuarioId);

    boolean existsByUsuarioIdAndConquistaId(Long usuarioId, String conquistaId);
}

