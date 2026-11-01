package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transação extraída de uma fatura de cartão de crédito")
public record TransacaoFaturaDTO(

    @Schema(example = "RESTAURANTE DO ZE")
    String descricao,

    @Schema(example = "150.00")
    BigDecimal valor,

    @Schema(example = "2026-01-15")
    LocalDate data,

    @Schema(example = "Alimentação")
    String categoria,

    @Schema(description = "Número da parcela atual, quando a compra é parcelada", example = "2")
    Integer numeroParcela,

    @Schema(description = "Total de parcelas da compra", example = "10")
    Integer totalParcelas,

    @Schema(description = "Valor total original da compra parcelada", example = "500.00")
    BigDecimal valorTotalCompra
) {
}
