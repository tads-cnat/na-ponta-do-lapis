package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findTop20ByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);

    Page<Notificacao> findByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId, Pageable pageable);

    long countByUsuarioIdAndLidaFalse(Long usuarioId);

    @Modifying
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.usuario.id = :usuarioId AND n.lida = false")
    int marcarTodasComoLidasPorUsuario(@Param("usuarioId") Long usuarioId);
}

