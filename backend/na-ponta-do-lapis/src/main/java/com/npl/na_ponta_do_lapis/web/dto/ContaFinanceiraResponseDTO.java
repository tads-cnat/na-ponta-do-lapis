package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;


public record ContaFinanceiraResponseDTO(Long id, String nome, BigDecimal saldo, TipoConta tipo, Long usuarioId) {

    public ContaFinanceiraResponseDTO(ContaFinanceira contaFinanceira){
        this(contaFinanceira.getId(), contaFinanceira.getNome(), contaFinanceira.getSaldo(), contaFinanceira.getTipo(), contaFinanceira.getUsuario().getId());
    }
}
