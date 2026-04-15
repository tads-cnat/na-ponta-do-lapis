package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;
import com.npl.na_ponta_do_lapis.entity.Usuario;

public record ContaFinanceiraDTO(String nome, BigDecimal saldo, TipoConta tipo, Usuario usuario) {

    public ContaFinanceira toEntity(){
        ContaFinanceira conta = new ContaFinanceira();
        conta.setNome(nome);
        conta.setSaldo(saldo);
        conta.setTipo(tipo);
        conta.setUsuario(usuario);
        return conta;
    }
}
