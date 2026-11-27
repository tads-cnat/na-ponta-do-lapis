package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PagarFaturaDTO(
        @NotNull(message = "Informe a conta de origem para pagamento.")
        Long contaOrigemId,

        @NotNull(message = "Informe o valor do pagamento.")
        @Positive(message = "O valor do pagamento deve ser maior que zero.")
        BigDecimal valor
) {
}

