package com.npl.na_ponta_do_lapis.service.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;


@Component
public class CalculadoraDivida implements CalculadoraMetaStrategy {

    @Override
    public TipoMeta getTipo() {
        return TipoMeta.DIVIDA;
    }

    @Override
    public double calcularProgresso(BigDecimal valorAtual, BigDecimal limiteDivida) {
        if (limiteDivida.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        double progresso = (valorAtual.divide(limiteDivida, 2, java.math.RoundingMode.HALF_UP).doubleValue()) * 100;
        return Math.min(progresso, 100.0);
    }

}
