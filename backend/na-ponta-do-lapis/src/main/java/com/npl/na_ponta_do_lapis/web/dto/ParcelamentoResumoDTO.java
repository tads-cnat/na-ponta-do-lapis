package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelamentoResumoDTO(
        String grupoId,
        String descricao,
        BigDecimal valorParcela,
        BigDecimal valorTotalRestante,
        int parcelasPagas,
        int parcelasRestantes,
        int totalParcelas,
        LocalDate proximaParcelaData,
        LocalDate ultimaParcelaData,
        String contaNome,
        String categoriaNome
) {
}
