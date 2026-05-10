package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContaFinanceiraDTO(

    @NotBlank(message = "O nome da conta não pode ser vazio")
    String nome,
    
    @NotNull(message = "O saldo é obrigatório")
    BigDecimal saldo,

    @NotBlank(message = "A cor não pode ser vazia")
    String cor
    ) {

    public ContaFinanceira toEntity(Usuario usuario){
        ContaFinanceira conta = new ContaFinanceira();
        conta.setNome(nome);
        conta.setSaldo(saldo);
        conta.setCor(cor);
        conta.setUsuario(usuario);
        return conta;
    }
}
