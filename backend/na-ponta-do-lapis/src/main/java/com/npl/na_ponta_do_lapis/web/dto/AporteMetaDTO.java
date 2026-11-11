package com.npl.na_ponta_do_lapis.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AporteMetaDTO(
        @Schema(example = "150.00")
        @NotNull(message = "Valor do aporte é obrigatório")
        @Positive(message = "Valor do aporte deve ser positivo")
        BigDecimal valor,

        @Schema(example = "1")
        Long contaOrigemId,

        @Schema(example = "Economia do final de semana")
        @Size(max = 255)
        String observacao
) {
}

