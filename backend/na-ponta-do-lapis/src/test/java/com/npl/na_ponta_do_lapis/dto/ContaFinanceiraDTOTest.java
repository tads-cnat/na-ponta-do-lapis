package com.npl.na_ponta_do_lapis.dto;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("ContaFinanceiraDTO (entrada)")
public class ContaFinanceiraDTOTest {

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

    private static ContaFinanceiraDTO dtoValido() {
        return new ContaFinanceiraDTO("Carteira", new BigDecimal("250.50"), "#FF0000", Moeda.BRL);
    }

    private static Set<String> mensagens(ContaFinanceiraDTO dto, String campo) {
        return validator.validate(dto).stream()
                .filter(v -> v.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Nested
    @DisplayName("Validação")
    class Validacao {

        @Test
        @DisplayName("DTO totalmente válido não gera violações")
        void dtoValidoNaoDeveGerarViolacoes() {
            assertThat(validator.validate(dtoValido())).isEmpty();
        }

        @Test
        @DisplayName("todos os campos nulos geram violações")
        void camposNulosDevemGerarViolacoes() {
            Set<String> campos = validator.validate(new ContaFinanceiraDTO(null, null, null, null)).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(Collectors.toSet());

            assertThat(campos).containsExactlyInAnyOrder("nome", "saldo", "cor", "moeda");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("nome nulo, vazio ou em branco é rejeitado")
        void deveRejeitarNomeInvalido(String nome) {
            assertThat(mensagens(new ContaFinanceiraDTO(nome, BigDecimal.ONE, "#FFFFFF", Moeda.BRL), "nome"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("saldo nulo é rejeitado")
        void deveRejeitarSaldoNulo() {
            assertThat(mensagens(new ContaFinanceiraDTO("Carteira", null, "#FFFFFF", Moeda.BRL), "saldo"))
                    .contains("O saldo é obrigatório");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("cor vazia ou em branco é rejeitada")
        void deveRejeitarCorInvalida(String cor) {
            assertThat(mensagens(new ContaFinanceiraDTO("Carteira", BigDecimal.ONE, cor, Moeda.BRL), "cor"))
                    .contains("A cor não pode ser vazia");
        }

        @Test
        @DisplayName("moeda nula é rejeitada")
        void deveRejeitarMoedaNula() {
            assertThat(mensagens(new ContaFinanceiraDTO("Carteira", BigDecimal.ONE, "#FFFFFF", null), "moeda"))
                    .contains("A moeda é obrigatória");
        }
    }

    @Nested
    @DisplayName("toEntity(Usuario)")
    class ToEntity {

        @Test
        @DisplayName("copia os dados do DTO e associa o usuário")
        void deveCopiarDadosEUsuario() {
            Usuario usuario = new Usuario();
            usuario.setId(7L);

            ContaFinanceira conta = dtoValido().toEntity(usuario);

            assertAll(
                    () -> assertThat(conta.getNome()).isEqualTo("Carteira"),
                    () -> assertThat(conta.getSaldo()).isEqualByComparingTo("250.50"),
                    () -> assertThat(conta.getCor()).isEqualTo("#FF0000"),
                    () -> assertThat(conta.getMoeda()).isEqualTo(Moeda.BRL),
                    () -> assertThat(conta.getUsuario()).isSameAs(usuario)
            );
        }

        @Test
        @DisplayName("não preenche o id controlado pelo servidor")
        void naoDevePreencherId() {
            assertThat(dtoValido().toEntity(new Usuario()).getId()).isNull();
        }
    }

    @Test
    @DisplayName("desserializa JSON em ContaFinanceiraDTO")
    void deveDesserializarJson() throws Exception {
        ContaFinanceiraDTO dto = new ObjectMapper().readValue(
                "{\"nome\":\"Carteira\",\"saldo\":250.50,\"cor\":\"#FF0000\",\"moeda\":\"BRL\"}",
                ContaFinanceiraDTO.class);

        assertAll(
                () -> assertThat(dto.nome()).isEqualTo("Carteira"),
                () -> assertThat(dto.saldo()).isEqualByComparingTo("250.50"),
                () -> assertThat(dto.cor()).isEqualTo("#FF0000"),
                () -> assertThat(dto.moeda()).isEqualTo(Moeda.BRL)
        );
    }
}
