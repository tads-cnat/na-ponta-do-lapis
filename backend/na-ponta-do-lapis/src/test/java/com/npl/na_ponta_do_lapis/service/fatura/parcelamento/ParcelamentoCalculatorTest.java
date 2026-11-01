package com.npl.na_ponta_do_lapis.service.fatura.parcelamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class ParcelamentoCalculatorTest {

    @Test
    void divideValorComRestoNaUltimaParcela() {
        List<BigDecimal> valores = ParcelamentoCalculator.calcularValores(new BigDecimal("100.00"), 3);

        assertThat(valores).containsExactly(
                new BigDecimal("33.33"),
                new BigDecimal("33.33"),
                new BigDecimal("33.34")
        );
        assertThat(valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void divideValorPequenoSemPerderCentavos() {
        List<BigDecimal> valores = ParcelamentoCalculator.calcularValores(new BigDecimal("0.10"), 3);

        assertThat(valores).containsExactly(
                new BigDecimal("0.03"),
                new BigDecimal("0.03"),
                new BigDecimal("0.04")
        );
        assertThat(valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("0.10");
    }

    @Test
    void calculaDatasMensaisRespeitandoFimDeMes() {
        LocalDateTime primeira = LocalDateTime.of(2026, 1, 31, 0, 0);

        List<LocalDateTime> datas = ParcelamentoCalculator.calcularDatas(primeira, 4);

        assertThat(datas).containsExactly(
                LocalDateTime.of(2026, 1, 31, 0, 0),
                LocalDateTime.of(2026, 2, 28, 0, 0),
                LocalDateTime.of(2026, 3, 31, 0, 0),
                LocalDateTime.of(2026, 4, 30, 0, 0)
        );
    }

    @Test
    void rejeitaEntradasInvalidas() {
        assertThatThrownBy(() -> ParcelamentoCalculator.calcularValores(null, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ParcelamentoCalculator.calcularValores(new BigDecimal("10.00"), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ParcelamentoCalculator.calcularDatas(null, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ParcelamentoCalculator.calcularDatas(LocalDateTime.now(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
