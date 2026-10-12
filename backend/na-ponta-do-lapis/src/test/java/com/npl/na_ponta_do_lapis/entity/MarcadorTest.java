package com.npl.na_ponta_do_lapis.entity;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("Marcador (entidade)")
class MarcadorTest {

    private static final String MSG_NOME_NULO = "Nome não pode ser nulo";
    private static final String MSG_COR_NULA = "Cor não pode ser nulo";
    private static final String MSG_VAZIO = "O campo não pode estar vazio";

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

    private static Marcador marcadorValido() {
        Marcador marcador = new Marcador();
        marcador.setNome("Trabalho");
        marcador.setCor("#FF0000");
        marcador.setUsuario(usuarioComId(1L));
        return marcador;
    }

    private static Marcador marcadorComId(Long id) {
        Marcador marcador = new Marcador();
        marcador.setId(id);
        return marcador;
    }

    /** Mensagens de violação apenas do campo informado. */
    private static Set<String> mensagens(Marcador marcador, String campo) {
        return validator.validate(marcador).stream()
                .filter(v -> v.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Nested
    @DisplayName("Construtor padrão")
    class ConstrutorPadrao {

        @Test
        @DisplayName("não preenche nenhum atributo")
        void naoDevePreencherAtributos() {
            Marcador marcador = new Marcador();

            assertAll(
                    () -> assertThat(marcador.getId()).isNull(),
                    () -> assertThat(marcador.getNome()).isNull(),
                    () -> assertThat(marcador.getCor()).isNull(),
                    () -> assertThat(marcador.getUsuario()).isNull()
            );
        }
    }

    @Test
    @DisplayName("setters e getters preservam os valores atribuídos")
    void settersEGettersDevemPreservarValores() {
        Marcador marcador = new Marcador();
        Usuario usuario = usuarioComId(2L);

        marcador.setId(10L);
        marcador.setNome("Estudos");
        marcador.setCor("#00FF00");
        marcador.setUsuario(usuario);

        assertAll(
                () -> assertThat(marcador.getId()).isEqualTo(10L),
                () -> assertThat(marcador.getNome()).isEqualTo("Estudos"),
                () -> assertThat(marcador.getCor()).isEqualTo("#00FF00"),
                () -> assertThat(marcador.getUsuario()).isSameAs(usuario)
        );
    }

    @Nested
    @DisplayName("Validação (Bean Validation)")
    class Validacao {

        @Test
        @DisplayName("marcador válido não gera violações")
        void marcadorValidoNaoDeveGerarViolacoes() {
            assertThat(validator.validate(marcadorValido())).isEmpty();
        }

        @Nested
        @DisplayName("nome")
        class Nome {

            @Test
            @DisplayName("null viola @NotNull e @NotBlank")
            void deveRejeitarNull() {
                Marcador marcador = marcadorValido();
                marcador.setNome(null);

                assertThat(mensagens(marcador, "nome"))
                        .containsExactlyInAnyOrder(MSG_NOME_NULO, MSG_VAZIO);
            }

            @ParameterizedTest(name = "nome \"{0}\" é rejeitado")
            @ValueSource(strings = {"", " ", "   "})
            @DisplayName("vazio ou só espaços viola apenas @NotBlank")
            void deveRejeitarVazioOuEmBranco(String nome) {
                Marcador marcador = marcadorValido();
                marcador.setNome(nome);

                assertThat(mensagens(marcador, "nome")).containsExactly(MSG_VAZIO);
            }

            @Test
            @DisplayName("não há @Size: 71 caracteres passam no Bean Validation (o limite de 70 é só do banco)")
            void tamanhoNaoEhValidadoPorBeanValidation() {
                Marcador marcador = marcadorValido();
                marcador.setNome("a".repeat(71));

                assertThat(mensagens(marcador, "nome")).isEmpty();
            }
        }

        @Nested
        @DisplayName("cor")
        class Cor {

            @Test
            @DisplayName("null viola @NotNull e @NotBlank")
            void deveRejeitarNull() {
                Marcador marcador = marcadorValido();
                marcador.setCor(null);

                assertThat(mensagens(marcador, "cor"))
                        .containsExactlyInAnyOrder(MSG_COR_NULA, MSG_VAZIO);
            }

            @ParameterizedTest(name = "cor \"{0}\" é rejeitada")
            @ValueSource(strings = {"", " ", "   "})
            @DisplayName("vazia ou só espaços viola apenas @NotBlank")
            void deveRejeitarVaziaOuEmBranco(String cor) {
                Marcador marcador = marcadorValido();
                marcador.setCor(cor);

                assertThat(mensagens(marcador, "cor")).containsExactly(MSG_VAZIO);
            }

            @Test
            @DisplayName("não há @Pattern: formato HEX só é validado no DTO")
            void formatoNaoEhValidadoNaEntidade() {
                Marcador marcador = marcadorValido();
                marcador.setCor("vermelho");

                assertThat(mensagens(marcador, "cor")).isEmpty();
            }
        }

        @Test
        @DisplayName("usuário nulo não viola Bean Validation (a obrigatoriedade é só do banco)")
        void usuarioNuloNaoGeraViolacao() {
            Marcador marcador = marcadorValido();
            marcador.setUsuario(null);

            assertThat(validator.validate(marcador)).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals e hashCode (baseados no id)")
    class EqualsHashCode {

        @Test
        @DisplayName("mesmo id: iguais, com o mesmo hashCode")
        void mesmoIdDeveSerIgual() {
            Marcador m1 = marcadorComId(10L);
            Marcador m2 = marcadorComId(10L);

            assertAll(
                    () -> assertThat(m1).isEqualTo(m2),
                    () -> assertThat(m1.hashCode()).isEqualTo(m2.hashCode())
            );
        }

        @Test
        @DisplayName("ids diferentes: não são iguais")
        void idsDiferentesNaoDevemSerIguais() {
            assertThat(marcadorComId(10L)).isNotEqualTo(marcadorComId(20L));
        }

        @Test
        @DisplayName("é igual a si mesmo")
        void deveSerReflexivo() {
            Marcador marcador = marcadorComId(10L);
            assertThat(marcador.equals(marcador)).isTrue();
        }

        @Test
        @DisplayName("não é igual a null nem a outro tipo")
        void naoDeveSerIgualANullOuOutroTipo() {
            Marcador marcador = marcadorComId(10L);

            assertAll(
                    () -> assertThat(marcador.equals(null)).isFalse(),
                    () -> assertThat(marcador.equals("outra classe")).isFalse()
            );
        }

        @Test
        @DisplayName("sem id, todos os marcadores são iguais entre si (comportamento atual)")
        void marcadoresSemIdSaoIguais() {
            Marcador primeiro = new Marcador();
            primeiro.setNome("Trabalho");
            Marcador segundo = new Marcador();
            segundo.setNome("Estudos");

            assertAll(
                    () -> assertThat(primeiro).isEqualTo(segundo),
                    () -> assertThat(primeiro.hashCode()).isEqualTo(segundo.hashCode())
            );
        }

        @Test
        @DisplayName("hashCode é estável entre chamadas")
        void hashCodeDeveSerEstavel() {
            Marcador marcador = marcadorComId(10L);
            assertThat(marcador.hashCode()).isEqualTo(marcador.hashCode());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("inclui id, nome e cor")
        void deveIncluirIdNomeECor() {
            Marcador marcador = new Marcador();
            marcador.setId(5L);
            marcador.setNome("Estudos");
            marcador.setCor("#00FF00");

            assertThat(marcador.toString()).isEqualTo("Marcador{id=5, nome='Estudos', cor='#00FF00'}");
        }

        @Test
        @DisplayName("não expõe o usuário")
        void naoDeveExporUsuario() {
            Marcador marcador = marcadorValido();
            marcador.setUsuario(usuarioComId(99L));

            assertThat(marcador.toString()).doesNotContain("usuario");
        }
    }
}
