package com.npl.na_ponta_do_lapis.web.dto;

public record OpenFinanceItemDTO(
        String itemId,
        String nomeInstituicao,
        String logoUrl,
        String status,
        String ultimaAtualizacao
) {
}

