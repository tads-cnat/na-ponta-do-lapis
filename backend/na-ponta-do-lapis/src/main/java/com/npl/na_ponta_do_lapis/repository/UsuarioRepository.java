package com.npl.na_ponta_do_lapis.repository;


import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Usuario findByUsername(String username);
    List<Usuario> findByPapelIn(List<Papel> papels);

    Optional<Usuario> findByEmail(String mail);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByRecuperacaoTokenHash(String recuperacaoTokenHash);

    Optional<Usuario> findByEmailVerificacaoTokenHash(String emailVerificacaoTokenHash);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.ativo IS NULL OR u.ativo = true")
    long contarAtivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.ativo = false")
    long contarSuspensos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE (u.ativo IS NULL OR u.ativo = true) AND (u.emailVerificado IS NULL OR u.emailVerificado = true)")
    long contarAtivosVerificados();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE (u.ativo IS NULL OR u.ativo = true) AND u.emailVerificado = false")
    long contarAtivosNaoVerificados();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.papel = :papel AND (u.ativo IS NULL OR u.ativo = true)")
    long contarAtivosPorPapel(@Param("papel") Papel papel);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.emailVerificado IS NULL OR u.emailVerificado = true")
    long contarEmailsVerificados();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.onboardingConcluido = true")
    long contarOnboardingsConcluidos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE COALESCE(u.criadoEm, u.termosAceitosEm) >= :desde")
    long contarCriadosDesde(@Param("desde") LocalDateTime desde);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE COALESCE(u.criadoEm, u.termosAceitosEm) >= :inicio AND COALESCE(u.criadoEm, u.termosAceitosEm) < :fim")
    long contarCriadosEntre(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT u FROM Usuario u WHERE u.ativo IS NULL OR u.ativo = true")
    List<Usuario> listarAtivos();

    @Modifying
    @Query("UPDATE Usuario u SET u.ultimoAcessoEm = :agora WHERE u.id = :id")
    int atualizarUltimoAcesso(@Param("id") Long id, @Param("agora") LocalDateTime agora);
}
