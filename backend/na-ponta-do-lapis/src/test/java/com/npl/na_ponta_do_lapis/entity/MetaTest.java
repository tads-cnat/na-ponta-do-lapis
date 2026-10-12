package com.npl.na_ponta_do_lapis.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

@DisplayName("Meta (entidade)")
class MetaTest {

    private static Usuario usuarioComId(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    @Nested
    @DisplayName("Construtor padrão")
    class ConstrutorPadrao {

        @Test
        @DisplayName("inicia valorAtual em zero")
        void deveIniciarValorAtualZero() {
            Meta meta = new Meta();
            assertThat(meta.getValorAtual()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("não preenche nenhum outro atributo")
        void naoDevePreencherOutrosAtributos() {
            Meta meta = new Meta();

            assertAll(
                    () -> assertThat(meta.getId()).isNull(),
                    () -> assertThat(meta.getNome()).isNull(),
                    () -> assertThat(meta.getDescricao()).isNull(),
                    () -> assertThat(meta.getValor()).isNull(),
                    () -> assertThat(meta.getFotoUrl()).isNull(),
                    () -> assertThat(meta.getDataInicio()).isNull(),
                    () -> assertThat(meta.getDataLimite()).isNull(),
                    () -> assertThat(meta.getDataEncerramento()).isNull(),
                    () -> assertThat(meta.getTipoMeta()).isNull(),
                    () -> assertThat(meta.getUsuario()).isNull(),
                    () -> assertThat(meta.getConta()).isNull()
            );
        }
    }

    @Nested
    @DisplayName("Construtor Meta(valor, nome, tipoMeta, usuario)")
    class ConstrutorParametrizado {

        private final Usuario usuario = usuarioComId(1L);
        private final BigDecimal valor = new BigDecimal("5000.00");

        @Test
        @DisplayName("atribui valor, nome e usuário")
        void deveAtribuirValorNomeEUsuario() {
            Meta meta = new Meta(valor, "Reserva", TipoMeta.POUPANCA, usuario);

            assertAll(
                    () -> assertThat(meta.getValor()).isEqualByComparingTo("5000.00"),
                    () -> assertThat(meta.getNome()).isEqualTo("Reserva"),
                    () -> assertThat(meta.getUsuario()).isSameAs(usuario)
            );
        }

        @Test
        @DisplayName("atribui o tipoMeta recebido")
        void deveAtribuirTipoMeta() {
            Meta meta = new Meta(valor, "Reserva", TipoMeta.DIVIDA, usuario);
            assertThat(meta.getTipoMeta()).isEqualTo(TipoMeta.DIVIDA);
        }

        @Test
        @DisplayName("inicia valorAtual em zero")
        void deveIniciarValorAtualEmZero() {
            Meta meta = new Meta(valor, "Reserva", TipoMeta.POUPANCA, usuario);
            assertThat(meta.getValorAtual()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("não preenche atributos opcionais nem gerenciados pelo banco")
        void naoDevePreencherOpcionais() {
            Meta meta = new Meta(valor, "Reserva", TipoMeta.POUPANCA, usuario);

            assertAll(
                    () -> assertThat(meta.getId()).isNull(),
                    () -> assertThat(meta.getDescricao()).isNull(),
                    () -> assertThat(meta.getConta()).isNull(),
                    () -> assertThat(meta.getDataLimite()).isNull()
            );
        }
    }

    @Test
    @DisplayName("setters e getters preservam os valores atribuídos")
    void settersEGettersDevemPreservarValores() {

        Meta meta = new Meta();
        Usuario usuario = usuarioComId(2L);
        ContaFinanceira conta = new ContaFinanceira();

        conta.setId(3L);
        meta.setId(10L);
        meta.setNome("Viagem");
        meta.setDescricao("Férias");
        meta.setValor(new BigDecimal("3000.00"));
        meta.setValorAtual(new BigDecimal("750.50"));
        meta.setFotoUrl("https://exemplo.com/foto.png");
        meta.setDataInicio(LocalDate.of(2026, 1, 10));
        meta.setDataLimite(LocalDate.of(2026, 12, 31));
        meta.setDataEncerramento(LocalDate.of(2026, 11, 30));
        meta.setTipoMeta(TipoMeta.PRAZO_FIXO);
        meta.setUsuario(usuario);
        meta.setConta(conta);

        assertAll(
                () -> assertThat(meta.getId()).isEqualTo(10L),
                () -> assertThat(meta.getNome()).isEqualTo("Viagem"),
                () -> assertThat(meta.getDescricao()).isEqualTo("Férias"),
                () -> assertThat(meta.getValor()).isEqualByComparingTo("3000.00"),
                () -> assertThat(meta.getValorAtual()).isEqualByComparingTo("750.50"),
                () -> assertThat(meta.getFotoUrl()).isEqualTo("https://exemplo.com/foto.png"),
                () -> assertThat(meta.getDataInicio()).isEqualTo(LocalDate.of(2026, 1, 10)),
                () -> assertThat(meta.getDataLimite()).isEqualTo(LocalDate.of(2026, 12, 31)),
                () -> assertThat(meta.getDataEncerramento()).isEqualTo(LocalDate.of(2026, 11, 30)),
                () -> assertThat(meta.getTipoMeta()).isEqualTo(TipoMeta.PRAZO_FIXO),
                () -> assertThat(meta.getUsuario()).isSameAs(usuario),
                () -> assertThat(meta.getConta()).isSameAs(conta)
        );
    }
}
