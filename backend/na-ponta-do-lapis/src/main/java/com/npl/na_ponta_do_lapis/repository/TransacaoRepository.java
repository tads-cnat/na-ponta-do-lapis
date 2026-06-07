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
}
