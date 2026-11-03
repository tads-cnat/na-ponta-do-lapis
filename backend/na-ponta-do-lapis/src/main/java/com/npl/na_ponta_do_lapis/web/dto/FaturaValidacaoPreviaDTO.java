package com.npl.na_ponta_do_lapis.web.dto;

public record FaturaValidacaoPreviaDTO(
        boolean valido,
        boolean protegidoPorSenha,
        String emissor,
        String codigo,
        String mensagem
) {}
