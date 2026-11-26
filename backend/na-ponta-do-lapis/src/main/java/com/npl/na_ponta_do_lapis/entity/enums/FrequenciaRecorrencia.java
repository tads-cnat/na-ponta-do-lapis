package com.npl.na_ponta_do_lapis.entity.enums;

import java.time.LocalDate;

public enum FrequenciaRecorrencia {
    SEMANAL("Semanal"),
    MENSAL("Mensal"),
    TRIMESTRAL("Trimestral"),
    ANUAL("Anual");

    private final String descricao;

    FrequenciaRecorrencia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate avancar(LocalDate dataBase, int diaReferencia) {
        return switch (this) {
            case SEMANAL -> dataBase.plusWeeks(1);
            case MENSAL -> ajustarDia(dataBase.plusMonths(1), diaReferencia);
            case TRIMESTRAL -> ajustarDia(dataBase.plusMonths(3), diaReferencia);
            case ANUAL -> ajustarDia(dataBase.plusYears(1), diaReferencia);
        };
    }

    private LocalDate ajustarDia(LocalDate data, int diaReferencia) {
        if (diaReferencia <= 0) {
            return data;
        }
        int diaAjustado = Math.min(diaReferencia, data.lengthOfMonth());
        return data.withDayOfMonth(diaAjustado);
    }
}
