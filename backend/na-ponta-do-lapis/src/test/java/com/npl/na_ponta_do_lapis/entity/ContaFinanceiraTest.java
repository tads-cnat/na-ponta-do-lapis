package com.npl.na_ponta_do_lapis.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.npl.na_ponta_do_lapis.entity.enums.Moeda;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("ContaFinanceira (entidade)")
public class ContaFinanceiraTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharFactory() {
        factory.close();
    }

    private static Usuario usuarioComId(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private static ContaFinanceira contaValida() {
        ContaFinanceira conta = new ContaFinanceira();
        conta.setNome("Carteira");
        conta.setSaldo(new BigDecimal("250.50"));
        conta.setCor("#FF0000");
        conta.setMoeda(Moeda.BRL);
        conta.setUsuario(usuarioComId(1L));
        return conta;
    }

    @Nested
    @DisplayName("Construtor padrão")
    class ConstrutorPadrao {

        @Test
        @DisplayName("não preenche nenhum atributo")
        void naoDevePreencherAtributos() {
            ContaFinanceira conta = new ContaFinanceira();

            assertAll(
                    () -> assertThat(conta.getId()).isNull(),
                    () -> assertThat(conta.getNome()).isNull(),
                    () -> assertThat(conta.getSaldo()).isNull(),
                    () -> assertThat(conta.getCor()).isNull(),
                    () -> assertThat(conta.getMoeda()).isNull(),
                    () -> assertThat(conta.getUsuario()).isNull()
            );
        }
    }

    @Test
    @DisplayName("conta válida não gera violações")
    void contaValidaNaoDeveGerarViolacoes() {
        assertThat(validator.validate(contaValida())).isEmpty();
    }

    @Test
    @DisplayName("conta sem dados gera violações para todos os campos obrigatórios")
    void contaSemDadosDeveGerarViolacoes() {
        Set<String> campos = validator.validate(new ContaFinanceira()).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(campos).containsExactlyInAnyOrder("nome", "saldo", "cor", "moeda", "usuario");
    }

    @Test
    @DisplayName("setters e getters preservam os valores atribuídos")
    void settersEGettersDevemPreservarValores() {
        ContaFinanceira conta = new ContaFinanceira();
        Usuario usuario = usuarioComId(2L);

        conta.setId(10L);
        conta.setNome("Conta corrente");
        conta.setSaldo(new BigDecimal("1500.75"));
        conta.setCor("#00FF00");
        conta.setMoeda(Moeda.USD);
        conta.setUsuario(usuario);

        assertAll(
                () -> assertThat(conta.getId()).isEqualTo(10L),
                () -> assertThat(conta.getNome()).isEqualTo("Conta corrente"),
                () -> assertThat(conta.getSaldo()).isEqualByComparingTo("1500.75"),
                () -> assertThat(conta.getCor()).isEqualTo("#00FF00"),
                () -> assertThat(conta.getMoeda()).isEqualTo(Moeda.USD),
                () -> assertThat(conta.getUsuario()).isSameAs(usuario)
        );
    }

    @Test
    @DisplayName("usa o id para igualdade e hashCode")
    void deveUsarIdParaIgualdadeEHashCode() {
        ContaFinanceira primeira = new ContaFinanceira();
        ContaFinanceira segunda = new ContaFinanceira();
        ContaFinanceira diferente = new ContaFinanceira();
        primeira.setId(7L);
        segunda.setId(7L);
        diferente.setId(8L);

        assertThat(primeira).isEqualTo(segunda);
        assertThat(primeira.hashCode()).isEqualTo(segunda.hashCode());
        assertThat(primeira).isNotEqualTo(diferente);
        assertThat(primeira).isNotEqualTo(null);
        assertThat(primeira).isNotEqualTo(new Object());
    }
}
