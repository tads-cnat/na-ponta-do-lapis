package com.npl.na_ponta_do_lapis.web.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoRequestDTO(
        @Size(min = 1, max = 254)
        @NotNull(message = "A Descrição não pode ser null!")
        @NotBlank(message = "O campo não pode estar vazio!")
        String descricao,
        @NotNull(message = "O valor não pode ser null!")
        @Positive(message = "O valor da transação deve ser maior que zero!")
        BigDecimal valor,

        @NotNull(message = "O Tipo transacao não pode ser null!")
        TipoTransacao tipo,

        EstadoTransacao estado,

        @NotNull(message = "ID categoria não pode ser null!")
        Long idCategoria,

        @NotNull(message = "ID conta financeira não pode ser null")
        Long idContaFinanceira,

        Long marcadorId,

        @Schema(description = "Data da transação em formato ISO 8601", example = "2026-05-09T19:41:49.000Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime dataHora
) {
}
