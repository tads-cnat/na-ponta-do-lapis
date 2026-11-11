package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("SELECT t FROM Transacao t WHERE t.contaFinanceira.usuario.email = :email ORDER BY t.dataHora desc ")
    List<Transacao> buscarTransacoesUsuarioLogado(String email);

    @Query("SELECT  t FROM Transacao t WHERE t.contaFinanceira.usuario.email = :email " +
            "AND LOWER(t.descricao) LIKE LOWER(CONCAT('%', :descricao, '%')) " +
             "ORDER BY t.dataHora DESC")
    List<Transacao> buscarPorDescricao(@Param("email") String email, @Param("descricao") String descricao);

    @Query("SELECT t FROM Transacao t WHERE t.contaFinanceira.usuario.familia.id = :familiaId ORDER BY t.dataHora DESC")
    List<Transacao> buscarTransacoesDaFamilia(@Param("familiaId") Long familiaId);

    @Query("SELECT t FROM Transacao t WHERE t.contaFinanceira.usuario.familia.id = :familiaId " +
            "AND LOWER(t.descricao) LIKE LOWER(CONCAT('%', :descricao, '%')) " +
            "ORDER BY t.dataHora DESC")
    List<Transacao> buscarTransacoesDaFamiliaPorDescricao(@Param("familiaId") Long familiaId, @Param("descricao") String descricao);

    List<Transacao> findByGrupoParcelamentoOrderByNumeroParcelaAsc(String grupoParcelamento);

    boolean existsByPluggyTransactionId(String pluggyTransactionId);

    java.util.Optional<Transacao> findByPluggyTransactionId(String pluggyTransactionId);

    @Query("""
        SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t
        WHERE t.tipo = com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao.DESPESA
        AND (:usuarioId IS NULL OR t.contaFinanceira.usuario.id = :usuarioId)
        AND (:familiaId IS NULL OR t.contaFinanceira.usuario.familia.id = :familiaId)
        AND (:contaId IS NULL OR t.contaFinanceira.id = :contaId)
        AND (:categoriaId IS NULL OR t.categoria.id = :categoriaId)
        AND t.dataHora >= :inicio
        AND t.dataHora <= :fim
    """)
    java.math.BigDecimal calcularTotalDespesas(
            @Param("usuarioId") Long usuarioId,
            @Param("familiaId") Long familiaId,
            @Param("contaId") Long contaId,
            @Param("categoriaId") Long categoriaId,
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fim") java.time.LocalDateTime fim
    );
}
