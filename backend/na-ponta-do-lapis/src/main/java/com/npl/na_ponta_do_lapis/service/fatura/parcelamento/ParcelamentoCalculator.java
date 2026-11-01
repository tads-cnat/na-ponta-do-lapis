package com.npl.na_ponta_do_lapis.service.fatura.parcelamento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cálculo puro de parcelas, sem dependência de banco.
 *
 * As N-1 primeiras parcelas usam truncamento em centavos; a última recebe o
 * resto para que a soma bata exatamente com o valor total.
 */
public final class ParcelamentoCalculator {

    private ParcelamentoCalculator() {
    }

    public static List<BigDecimal> calcularValores(BigDecimal valorTotal, int totalParcelas) {
        if (valorTotal == null || totalParcelas < 1) {
            throw new IllegalArgumentException("Valor total e número de parcelas são obrigatórios.");
        }

        BigDecimal base = valorTotal.divide(BigDecimal.valueOf(totalParcelas), 2, RoundingMode.DOWN);
        BigDecimal acumulado = BigDecimal.ZERO;
        List<BigDecimal> valores = new ArrayList<>(totalParcelas);

        for (int parcela = 1; parcela <= totalParcelas; parcela++) {
            BigDecimal valor = parcela == totalParcelas
                    ? valorTotal.subtract(acumulado)
                    : base;
            valores.add(valor.setScale(2, RoundingMode.HALF_UP));
            acumulado = acumulado.add(valor);
        }
        return valores;
    }

    public static List<LocalDateTime> calcularDatas(LocalDateTime primeira, int totalParcelas) {
        if (primeira == null || totalParcelas < 1) {
            throw new IllegalArgumentException("Data inicial e número de parcelas são obrigatórios.");
        }
        List<LocalDateTime> datas = new ArrayList<>(totalParcelas);
        for (int parcela = 0; parcela < totalParcelas; parcela++) {
            datas.add(primeira.plusMonths(parcela));
        }
        return datas;
    }
}
