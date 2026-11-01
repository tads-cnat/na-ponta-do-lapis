package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Cadastro manual de uma compra parcelada")
public record CompraParceladaRequestDTO(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 254)
    String descricao,

    @NotNull(message = "O valor total é obrigatório")
    @Positive(message = "O valor total deve ser maior que zero")
    BigDecimal valorTotal,

    @NotNull(message = "O número de parcelas é obrigatório")
    @Min(value = 2, message = "A compra deve ter pelo menos 2 parcelas")
    @Max(value = 60, message = "A compra pode ter no máximo 60 parcelas")
    Integer totalParcelas,

    @NotNull(message = "A data da primeira parcela é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dataPrimeiraParcela,

    @NotNull(message = "A conta financeira é obrigatória")
    Long idContaFinanceira,

    @NotNull(message = "A categoria é obrigatória")
    Long idCategoria,

    Long marcadorId
) {
}
