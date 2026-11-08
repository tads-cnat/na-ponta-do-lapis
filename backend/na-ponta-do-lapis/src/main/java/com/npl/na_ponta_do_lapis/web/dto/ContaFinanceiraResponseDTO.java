package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;

public record ContaFinanceiraResponseDTO(
        Long id,
        String nome,
        BigDecimal saldo,
        String cor,
        Moeda moeda,
        UsuarioResumoDTO usuario,
        boolean sincronizada,
        String pluggyAccountId,
        TipoConta tipoConta,
        BigDecimal limiteTotal,
        BigDecimal limiteDisponivel,
        BigDecimal valorFaturaFechada,
        BigDecimal valorFaturaAberta,
        LocalDate dataVencimentoFatura
) {

    public ContaFinanceiraResponseDTO(ContaFinanceira contaFinanceira){
        this(
            contaFinanceira.getId(),
            contaFinanceira.getNome(),
            contaFinanceira.getSaldo(),
            contaFinanceira.getCor(),
            contaFinanceira.getMoeda(),
            new UsuarioResumoDTO(contaFinanceira.getUsuario().getId(), contaFinanceira.getUsuario().getNome()),
            contaFinanceira.isSincronizada(),
            contaFinanceira.getPluggyAccountId(),
            contaFinanceira.getTipoConta(),
            contaFinanceira.getLimiteTotal(),
            contaFinanceira.getLimiteDisponivel(),
            contaFinanceira.getValorFaturaFechada(),
            contaFinanceira.getValorFaturaAberta(),
            contaFinanceira.getDataVencimentoFatura()
        );
    }
}
