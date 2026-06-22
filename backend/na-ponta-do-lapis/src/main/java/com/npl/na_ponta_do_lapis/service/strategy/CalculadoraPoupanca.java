package com.npl.na_ponta_do_lapis.service.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

@Component
public class CalculadoraPoupanca implements CalculadoraMetaStrategy {

    @Override
    public TipoMeta getTipo() {
        return TipoMeta.POUPANCA;
    }

    @Override
    public double calcularProgresso(BigDecimal valorAtual, BigDecimal valorMeta) {
        if (valorMeta.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        double progresso = (valorAtual.divide(valorMeta, 2, java.math.RoundingMode.HALF_UP).doubleValue()) * 100;
        return Math.min(progresso, 100.0);
    }

}
