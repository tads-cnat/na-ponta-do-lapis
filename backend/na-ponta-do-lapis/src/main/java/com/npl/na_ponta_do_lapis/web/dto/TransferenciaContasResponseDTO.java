package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

public record TransferenciaContasResponseDTO(
        ContaFinanceiraResponseDTO contaOrigem,
        ContaFinanceiraResponseDTO contaDestino,
        BigDecimal valor
) {
}

