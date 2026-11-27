package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.AssinaturaRecorrente;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.FrequenciaRecorrencia;
import com.npl.na_ponta_do_lapis.entity.enums.StatusRecorrencia;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record AssinaturaRecorrenteResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        BigDecimal valorMensalEquivalente,
        TipoTransacao tipo,
        FrequenciaRecorrencia frequencia,
        StatusRecorrencia status,
        EstadoTransacao estadoGeracao,
        Integer diaCobranca,
        LocalDate proximaCobranca,
        LocalDate ultimaCobranca,
        Long contaId,
        String contaNome,
        Long categoriaId,
        String categoriaNome,
        Long marcadorId,
        String marcadorNome
) {
    public AssinaturaRecorrenteResponseDTO(AssinaturaRecorrente a) {
        this(
                a.getId(),
                a.getDescricao(),
                a.getValor(),
                calcularMensalEquivalente(a.getValor(), a.getFrequencia()),
                a.getTipo(),
                a.getFrequencia(),
                a.getStatus(),
                a.getEstadoGeracao(),
                a.getDiaCobranca(),
                a.getProximaCobranca(),
                a.getUltimaCobranca(),
                a.getContaFinanceira() != null ? a.getContaFinanceira().getId() : null,
                a.getContaFinanceira() != null ? a.getContaFinanceira().getNome() : null,
                a.getCategoria() != null ? a.getCategoria().getId() : null,
                a.getCategoria() != null ? a.getCategoria().getNome() : null,
                a.getMarcador() != null ? a.getMarcador().getId() : null,
                a.getMarcador() != null ? a.getMarcador().getNome() : null
        );
    }

    public static BigDecimal calcularMensalEquivalente(BigDecimal valor, FrequenciaRecorrencia freq) {
        if (valor == null || freq == null) {
            return BigDecimal.ZERO;
        }
        return switch (freq) {
            case SEMANAL -> valor.multiply(new BigDecimal("4.33")).setScale(2, RoundingMode.HALF_UP);
            case MENSAL -> valor.setScale(2, RoundingMode.HALF_UP);
            case TRIMESTRAL -> valor.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
            case ANUAL -> valor.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        };
    }
}
