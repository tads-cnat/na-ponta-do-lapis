package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.util.Optional;

import com.npl.na_ponta_do_lapis.entity.enums.Moeda;

public record ContaFinanceiraPatchDTO(

    Optional<String> nome,
    Optional<BigDecimal> saldo,
    Optional<String> cor,
    Optional<Moeda> moeda,
    Optional<Long> usuarioId) {

    public ContaFinanceiraPatchDTO(String nome, BigDecimal saldo, String cor, Moeda moeda, Long usuarioId) {
        this(
            Optional.ofNullable(nome),
            Optional.ofNullable(saldo),
            Optional.ofNullable(cor),
            Optional.ofNullable(moeda),
            Optional.ofNullable(usuarioId)
        );
    }
}
