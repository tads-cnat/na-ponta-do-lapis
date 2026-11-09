package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContaFinanceiraDTO(

    @NotBlank(message = "O nome da conta não pode ser vazio")
    String nome,
    
    @NotNull(message = "O saldo é obrigatório")
    BigDecimal saldo,

    @NotBlank(message = "A cor não pode ser vazia")
    String cor,

    @NotNull(message = "A moeda é obrigatória")
    Moeda moeda,

    TipoConta tipoConta,

    BigDecimal limiteTotal
    ) {

    public ContaFinanceiraDTO(String nome, BigDecimal saldo, String cor, Moeda moeda) {
        this(nome, saldo, cor, moeda, TipoConta.CONTA_CORRENTE, null);
    }

    public ContaFinanceira toEntity(Usuario usuario){
        ContaFinanceira conta = new ContaFinanceira();
        conta.setNome(nome);
        conta.setSaldo(saldo);
        conta.setCor(cor);
        conta.setMoeda(moeda);
        conta.setUsuario(usuario);
        conta.setTipoConta(tipoConta != null ? tipoConta : TipoConta.CONTA_CORRENTE);
        conta.setLimiteTotal(limiteTotal);
        if (tipoConta == TipoConta.CARTAO_CREDITO && limiteTotal != null && limiteTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal gasto = saldo != null ? saldo.abs() : BigDecimal.ZERO;
            conta.setLimiteDisponivel(limiteTotal.subtract(gasto));
        }
        return conta;
    }
}
