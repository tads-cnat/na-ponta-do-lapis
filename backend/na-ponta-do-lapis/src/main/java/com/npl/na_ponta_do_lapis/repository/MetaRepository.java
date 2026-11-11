package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.enums.StatusMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetaRepository extends JpaRepository<Meta, Long> {

    List<Meta> findByUsuarioId(Long usuarioId);

    long countByUsuarioId(Long usuarioId);

    long countByUsuarioIdAndStatus(Long usuarioId, StatusMeta status);

    @Query("SELECT m FROM Meta m WHERE m.usuario.id = :usuarioId OR (m.familia IS NOT NULL AND m.familia.id = :familiaId)")
    List<Meta> findByUsuarioIdOuFamiliaId(@Param("usuarioId") Long usuarioId, @Param("familiaId") Long familiaId);

    @Query("SELECT m FROM Meta m WHERE m.tipoMeta = 'GASTO' AND (m.usuario.id = :usuarioId OR (m.familia IS NOT NULL AND m.familia.id = :familiaId))")
    List<Meta> findMetasDeGastoPorUsuarioOuFamilia(@Param("usuarioId") Long usuarioId, @Param("familiaId") Long familiaId);
}