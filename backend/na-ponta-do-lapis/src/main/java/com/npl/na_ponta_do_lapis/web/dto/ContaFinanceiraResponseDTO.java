package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;


public record ContaFinanceiraResponseDTO(Long id, String nome, BigDecimal saldo, String cor, UsuarioResumoDTO usuario) {

    public ContaFinanceiraResponseDTO(ContaFinanceira contaFinanceira){
        this(
            contaFinanceira.getId(),
            contaFinanceira.getNome(),
            contaFinanceira.getSaldo(),
            contaFinanceira.getCor(),
            new UsuarioResumoDTO(contaFinanceira.getUsuario().getId(), contaFinanceira.getUsuario().getNome())
        );
    }
}
