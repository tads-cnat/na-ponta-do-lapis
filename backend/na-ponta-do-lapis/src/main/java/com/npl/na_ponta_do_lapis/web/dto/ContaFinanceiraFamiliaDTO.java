package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;

import java.math.BigDecimal;

public record ContaFinanceiraFamiliaDTO(
    Long id,
    String nome,
    BigDecimal saldo,
    String cor,
    String moeda,
    String membroNome,
    String membroUsername
    ) {
    public ContaFinanceiraFamiliaDTO(ContaFinanceira c) {
        this(
            c.getId(),
            c.getNome(),
            c.getSaldo(),
            c.getCor(),
            c.getMoeda().toString(),
            c.getUsuario().getNome(),
            c.getUsuario().getUsername()
        );
    }
}
