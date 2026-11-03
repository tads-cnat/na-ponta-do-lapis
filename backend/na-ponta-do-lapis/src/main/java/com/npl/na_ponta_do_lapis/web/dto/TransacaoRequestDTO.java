package com.npl.na_ponta_do_lapis.web.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

        @NotNull(message = "Data não pode ser null!")
        @Schema(description = "Data da transação em formato ISO 8601", example = "2026-05-09T19:41:49")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        LocalDateTime dataHora,

        @Schema(description = "Indica se a transação é parcelada. Quando true, 'valor' é o valor TOTAL e 'dataHora' é a data da primeira parcela")
        Boolean parcelado,

        @Min(value = 2, message = "A compra deve ter pelo menos 2 parcelas")
        @Max(value = 60, message = "A compra pode ter no máximo 60 parcelas")
        @Schema(description = "Número de parcelas (obrigatório quando parcelado = true)")
        Integer totalParcelas,

        @Min(value = 1, message = "A parcela atual deve ser no mínimo 1")
        @Schema(description = "Parcela atual; usada ao converter uma transação existente em parcelada (edição)")
        Integer numeroParcela,

        @Positive(message = "O valor total da compra deve ser maior que zero!")
        @Schema(description = "Valor total real da compra (opcional, edição). Se omitido, usa valor × totalParcelas")
        BigDecimal valorTotalCompra,

        @Schema(description = "Na conversão em parcelada, cria também as parcelas anteriores à atual (realizadas, sem alterar o saldo)")
        Boolean criarParcelasAnteriores
) {
    /** Conversão/criação com parcela atual (sem valor total nem parcelas anteriores). */
    public TransacaoRequestDTO(String descricao, BigDecimal valor, TipoTransacao tipo, EstadoTransacao estado,
                               Long idCategoria, Long idContaFinanceira, Long marcadorId, LocalDateTime dataHora,
                               Boolean parcelado, Integer totalParcelas, Integer numeroParcela) {
        this(descricao, valor, tipo, estado, idCategoria, idContaFinanceira, marcadorId, dataHora,
                parcelado, totalParcelas, numeroParcela, null, false);
    }

    /** Criação com parcelamento opcional. */
    public TransacaoRequestDTO(String descricao, BigDecimal valor, TipoTransacao tipo, EstadoTransacao estado,
                               Long idCategoria, Long idContaFinanceira, Long marcadorId, LocalDateTime dataHora,
                               Boolean parcelado, Integer totalParcelas) {
        this(descricao, valor, tipo, estado, idCategoria, idContaFinanceira, marcadorId, dataHora,
                parcelado, totalParcelas, null, null, false);
    }

    /** Transação simples (não parcelada). */
    public TransacaoRequestDTO(String descricao, BigDecimal valor, TipoTransacao tipo, EstadoTransacao estado,
                               Long idCategoria, Long idContaFinanceira, Long marcadorId, LocalDateTime dataHora) {
        this(descricao, valor, tipo, estado, idCategoria, idContaFinanceira, marcadorId, dataHora,
                false, null, null, null, false);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isCompraParcelada() {
        return Boolean.TRUE.equals(parcelado);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isCriarAnteriores() {
        return Boolean.TRUE.equals(criarParcelasAnteriores);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @AssertTrue(message = "Informe o número de parcelas para uma compra parcelada")
    public boolean isTotalParcelasInformado() {
        return !isCompraParcelada() || totalParcelas != null;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @AssertTrue(message = "A parcela atual não pode ser maior que o total de parcelas")
    public boolean isNumeroParcelaValido() {
        return !isCompraParcelada() || numeroParcela == null || totalParcelas == null
                || numeroParcela <= totalParcelas;
    }
}
