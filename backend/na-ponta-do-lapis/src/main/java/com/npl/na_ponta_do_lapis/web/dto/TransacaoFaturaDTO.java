package com.npl.na_ponta_do_lapis.web.dto;

public record TransacaoFaturaDTO(
    String descricao,
    Double valor,
    String data,
    String categoria
) {}