package com.npl.na_ponta_do_lapis.web.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacoesResponseDTO(
        String descricao,
        BigDecimal valor,
        TipoTransacao tipo,
        Long idCategoria,
        Long idContaFinanceira,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHora
) {
}

