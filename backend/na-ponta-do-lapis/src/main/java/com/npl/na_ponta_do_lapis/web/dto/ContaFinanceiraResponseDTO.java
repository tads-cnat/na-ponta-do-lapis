package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;


public record ContaFinanceiraResponseDTO(Long id, String nome, BigDecimal saldo, String cor, Moeda moeda, UsuarioResumoDTO usuario) {

    public ContaFinanceiraResponseDTO(ContaFinanceira contaFinanceira){
        this(
            contaFinanceira.getId(),
            contaFinanceira.getNome(),
            contaFinanceira.getSaldo(),
            contaFinanceira.getCor(),
            contaFinanceira.getMoeda(),
            new UsuarioResumoDTO(contaFinanceira.getUsuario().getId(), contaFinanceira.getUsuario().getNome())
        );
    }
}
