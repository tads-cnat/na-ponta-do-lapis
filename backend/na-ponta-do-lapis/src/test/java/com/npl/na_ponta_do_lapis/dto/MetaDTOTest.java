package com.npl.na_ponta_do_lapis.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;


@DisplayName("MetaDTO (entrada)")
class MetaDTOTest {

    private static final String MSG_NOME_TAMANHO = "Nome deve ter entre 3 e 50 caracteres";
    private static final String MSG_NOME_OBRIGATORIO = "Nome é obrigatorio";
    private static final String MSG_VALOR_OBRIGATORIO = "Valor é obrigatorio";
    private static final String MSG_VALOR_POSITIVO = "Valor deve ser positivo";
    private static final String MSG_DATA_FUTURO = "Data limite deve ser no futuro";
    private static final String MSG_TIPO_OBRIGATORIO = "Tipo meta é Obrigatorio";

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

    private static MetaDTO dtoValido() {
        return new MetaDTO("Reserva de Emergência", "Guardar dinheiro para imprevistos",
                new BigDecimal("5000.00"), "https://link-da-foto.com/image.png",
                LocalDate.now().plusDays(30), TipoMeta.POUPANCA, 1L);
    }

    private static MetaDTO comNome(String nome) {
        MetaDTO b = dtoValido();
        return new MetaDTO(nome, b.descricao(), b.valor(), b.fotoUrl(), b.dataLimite(), b.tipoMeta(), b.contaId());
    }

    private static MetaDTO comValor(BigDecimal valor) {
        MetaDTO b = dtoValido();
        return new MetaDTO(b.nome(), b.descricao(), valor, b.fotoUrl(), b.dataLimite(), b.tipoMeta(), b.contaId());
    }

    private static MetaDTO comDataLimite(LocalDate dataLimite) {
        MetaDTO b = dtoValido();
        return new MetaDTO(b.nome(), b.descricao(), b.valor(), b.fotoUrl(), dataLimite, b.tipoMeta(), b.contaId());
    }

    private static MetaDTO comTipoMeta(TipoMeta tipoMeta) {
        MetaDTO b = dtoValido();
        return new MetaDTO(b.nome(), b.descricao(), b.valor(), b.fotoUrl(), b.dataLimite(), tipoMeta, b.contaId());
    }

    private static Set<String> mensagens(MetaDTO dto, String campo) {
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
        @DisplayName("campos opcionais aceitam null")
        void camposOpcionaisDevemAceitarNull() {
            MetaDTO dto = new MetaDTO("Reserva",
                            null, new BigDecimal("100.00"), null, null,
                            TipoMeta.GASTO, null);
            assertThat(validator.validate(dto)).isEmpty();
        }

        @Test
        @DisplayName("vários campos inválidos geram violações em cada um deles")
        void variosCamposInvalidos() {
            MetaDTO dto = new MetaDTO(null, null, null, null, null, null, null);

            Set<String> campos = validator.validate(dto).stream()
                    .map(v -> v.getPropertyPath().toString())
                    .collect(Collectors.toSet());

            assertThat(campos).containsExactlyInAnyOrder("nome", "valor", "tipoMeta");
        }

        @Nested
        @DisplayName("nome (3..50 caracteres, não vazio)")
        class Nome {

            @ParameterizedTest(name = "{0} caracteres é aceito")
            @ValueSource(ints = {3, 50})
            void deveAceitarTamanhoDentroLimite(int tamanho) {
                assertThat(mensagens(comNome("a".repeat(tamanho)), "nome")).isEmpty();
            }

            @ParameterizedTest(name = "{0} caracteres é rejeitado")
            @ValueSource(ints = {2, 51})
            void deveRejeitarTamanhoForaLimite(int tamanho) {
                assertThat(mensagens(comNome("a".repeat(tamanho)), "nome"))
                        .containsExactly(MSG_NOME_TAMANHO);
            }

            @Test
            @DisplayName("null viola apenas a obrigatoriedade (@Size ignora null)")
            void deveRejeitarNull() {
                assertThat(mensagens(comNome(null), "nome")).containsExactly(MSG_NOME_OBRIGATORIO);
            }

            @Test
            @DisplayName("string vazia viola obrigatoriedade E tamanho")
            void deveRejeitarStringVazia() {
                assertThat(mensagens(comNome(""), "nome"))
                        .containsExactlyInAnyOrder(MSG_NOME_OBRIGATORIO, MSG_NOME_TAMANHO);
            }

            @Test
            @DisplayName("só espaços (3+) viola obrigatoriedade mas passa no tamanho")
            void deveRejeitarApenasEspacos() {
                assertThat(mensagens(comNome("   "), "nome")).containsExactly(MSG_NOME_OBRIGATORIO);
            }
        }

        @Nested
        @DisplayName("valor (obrigatório, > 0)")
        class Valor {

            @Test
            @DisplayName("null viola apenas a obrigatoriedade")
            void deveRejeitarNull() {
                assertThat(mensagens(comValor(null), "valor")).containsExactly(MSG_VALOR_OBRIGATORIO);
            }

            @ParameterizedTest(name = "{valor} é rejeitado")
            @ValueSource(strings = {"0", "-0.01"})
            void deveRejeitarZeroENegativos(String valor) {
                assertThat(mensagens(comValor(new BigDecimal(valor)), "valor"))
                        .containsExactly(MSG_VALOR_POSITIVO);
            }

            @ParameterizedTest(name = "{valor} é aceito")
            @ValueSource(strings = {"0.01"})
            void deveAceitarPositivos(String valor) {
                assertThat(mensagens(comValor(new BigDecimal(valor)), "valor")).isEmpty();
            }
        }

        @Nested
        @DisplayName("dataLimite")
        class DataLimite {

            @Test
            @DisplayName("null é aceito")
            void deveAceitarNull() {
                assertThat(mensagens(comDataLimite(null), "dataLimite")).isEmpty();
            }

            @ParameterizedTest(name = "hoje {0} dia(s) é rejeitado")
            @ValueSource(longs = {-1, 0})
            void deveRejeitarHojeEPassado(long dias) {
                assertThat(mensagens(comDataLimite(LocalDate.now().plusDays(dias)), "dataLimite"))
                        .containsExactly(MSG_DATA_FUTURO);
            }

            @ParameterizedTest(name = "hoje +{0} dia(s) é aceito")
            @ValueSource(longs = {1})
            void deveAceitarFuturo(long dias) {
                assertThat(mensagens(comDataLimite(LocalDate.now().plusDays(dias)), "dataLimite")).isEmpty();
            }
        }

        @Nested
        @DisplayName("tipoMeta (obrigatório)")
        class TipoMetaCampo {

            @Test
            @DisplayName("null é rejeitado")
            void deveRejeitarNull() {
                assertThat(mensagens(comTipoMeta(null), "tipoMeta")).containsExactly(MSG_TIPO_OBRIGATORIO);
            }

            @ParameterizedTest(name = "{0} é aceito")
            @EnumSource(TipoMeta.class)
            void deveAceitarTodosOsTipos(TipoMeta tipo) {
                assertThat(mensagens(comTipoMeta(tipo), "tipoMeta")).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntity {

        @Test
        @DisplayName("copia os campos informados no DTO")
        void deveCopiarCamposInformados() {
            MetaDTO dto = dtoValido();

            Meta meta = dto.toEntity();

            assertAll(
                    () -> assertThat(meta.getNome()).isEqualTo(dto.nome()),
                    () -> assertThat(meta.getDescricao()).isEqualTo(dto.descricao()),
                    () -> assertThat(meta.getValor()).isEqualByComparingTo(dto.valor()),
                    () -> assertThat(meta.getFotoUrl()).isEqualTo(dto.fotoUrl()),
                    () -> assertThat(meta.getDataLimite()).isEqualTo(dto.dataLimite()),
                    () -> assertThat(meta.getTipoMeta()).isEqualTo(dto.tipoMeta())
            );
        }

        @Test
        @DisplayName("sempre inicia valorAtual em zero")
        void deveIniciarValorAtualZero() {
            assertThat(dtoValido().toEntity().getValorAtual()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("não preenche atributos controlados pelo servidor")
        void naoDevePreencherAtributosServidor() {
            Meta meta = dtoValido().toEntity();

            assertAll(
                    () -> assertThat(meta.getId()).isNull(),
                    () -> assertThat(meta.getUsuario()).isNull(),
                    () -> assertThat(meta.getDataInicio()).isNull(),
                    () -> assertThat(meta.getDataEncerramento()).isNull()
            );
        }

        @Test
        @DisplayName("não converte contaId em conta")
        void naoDeveConverterContaIdEmConta() {
            MetaDTO dto = dtoValido();
            assertThat(dto.toEntity().getConta()).isNull();
        }

        @Test
        @DisplayName("aceita campos opcionais nulos sem lançar exceção")
        void deveAceitarOpcionaisNulos() {
            MetaDTO dto = new MetaDTO("Reserva", null, new BigDecimal("100.00"), null, null,
                    TipoMeta.GASTO, null);

            Meta meta = dto.toEntity();

            assertAll(
                    () -> assertThat(meta.getDescricao()).isNull(),
                    () -> assertThat(meta.getFotoUrl()).isNull(),
                    () -> assertThat(meta.getDataLimite()).isNull()
            );
        }
    }
}
