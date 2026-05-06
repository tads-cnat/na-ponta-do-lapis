package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("SELECT t FROM Transacao t WHERE t.contaFinanceira.usuario.email = :email")
    List<Transacao> buscarTransacoesUsuarioLogado(String email);
}
