package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transação extraída de uma fatura de cartão de crédito")
public record TransacaoFaturaDTO(

    @Schema(example = "RESTAURANTE DO ZE")
    String descricao,

    @Schema(example = "150.00")
    BigDecimal valor,

    @Schema(example = "15/01/2026")
    LocalDateTime data,

    @Schema(example = "Alimentacao")
    String categoria
) {}