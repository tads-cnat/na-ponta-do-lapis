package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

public record CotacaoResponseDTO(
        String moeda,
        BigDecimal compra,
        BigDecimal venda
) {

    public CotacaoResponseDTO(CotacaoApiDTO dto) {
        this(
                dto.code() + "-" + dto.codein(),
                new BigDecimal(dto.bid()),
                new BigDecimal(dto.ask())
        );
    }
}