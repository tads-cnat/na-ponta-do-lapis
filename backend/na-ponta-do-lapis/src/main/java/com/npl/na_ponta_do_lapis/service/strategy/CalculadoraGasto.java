package com.npl.na_ponta_do_lapis.service.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

@Component
public class CalculadoraGasto implements CalculadoraMetaStrategy {

    @Override
    public TipoMeta getTipo() {
        return TipoMeta.GASTO;
    }

    @Override
    public double calcularProgresso(BigDecimal valorAtual, BigDecimal limiteGasto) {
        if (limiteGasto.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        double progresso = (valorAtual.divide(limiteGasto, 2, java.math.RoundingMode.HALF_UP).doubleValue()) * 100;
        return Math.min(progresso, 100.0);
    }

}
