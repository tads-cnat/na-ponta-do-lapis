package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.FrequenciaRecorrencia;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssinaturaRecorrenteRequestDTO(
        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "Tipo é obrigatório")
        TipoTransacao tipo,

        @NotNull(message = "Frequência é obrigatória")
        FrequenciaRecorrencia frequencia,

        EstadoTransacao estadoGeracao,

        @NotNull(message = "Data da próxima cobrança é obrigatória")
        LocalDate proximaCobranca,

        @NotNull(message = "Conta financeira é obrigatória")
        Long contaId,

        @NotNull(message = "Categoria é obrigatória")
        Long categoriaId,

        Long marcadorId,

        Boolean lancarPrimeiraAgora
) {
}
