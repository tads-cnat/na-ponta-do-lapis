package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.util.Optional;

import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;

public record ContaFinanceiraPatchDTO(

    Optional<String> nome,
    Optional<BigDecimal> saldo,
    Optional<TipoConta> tipo,
    Optional<Long> usuarioId) {

    public ContaFinanceiraPatchDTO(String nome, BigDecimal saldo, TipoConta tipo, Long usuarioId) {
        this(
            Optional.ofNullable(nome),
            Optional.ofNullable(saldo),
            Optional.ofNullable(tipo),
            Optional.ofNullable(usuarioId)
        );
    }
}
