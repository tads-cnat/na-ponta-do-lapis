package com.npl.na_ponta_do_lapis.tests.Meta.entity.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

@DisplayName("TipoMeta (enum)")
class TipoMetaTest {

    @ParameterizedTest(name = "{0} -> \"{1}\"")
    @CsvSource({
            "POUPANCA, Poupança",
            "GASTO, Gasto",
            "DIVIDA, Dívida",
            "PRAZO_FIXO, Prazo Fixo"
    })

    @DisplayName("getNome() retorna o rótulo legível de cada constante")
    void getNomeDeveRetornarRotuloLegivel(TipoMeta tipo, String esperado) {
        assertThat(tipo.getNome()).isEqualTo(esperado);
    }

    @Test
    @DisplayName("nomes das constantes são estáveis (persistidos como STRING e expostos na API)")
    void nomesDasConstantesDevemSerEstaveis() {
        assertThat(TipoMeta.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("POUPANCA", "GASTO", "DIVIDA", "PRAZO_FIXO");
    }

    @Test
    @DisplayName("valueOf() falha para nome inexistente")
    void valueOfDeveFalharParaNomeInexistente() {
        assertThatThrownBy(() -> TipoMeta.valueOf("INVESTIMENTO"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
