package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferenciaContasDTO(
        @NotNull(message = "Informe a conta de origem.")
        Long contaOrigemId,

        @NotNull(message = "Informe a conta de destino.")
        Long contaDestinoId,

        @NotNull(message = "Informe o valor da transferência.")
        @Positive(message = "O valor deve ser maior que zero.")
        BigDecimal valor,

        String descricao
) {
}

