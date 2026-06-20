package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

/**
 * Dado de cotação exposto pela nossa API.
 *
 * Campos enxutos — só o que a tela realmente usa: moeda, compra, venda.
 * Construído diretamente a partir do CotacaoApiResponseDTO (o JSON cru
 * da AwesomeAPI), sem passar por banco de dados.
 */
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