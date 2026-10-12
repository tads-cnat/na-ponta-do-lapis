package com.npl.na_ponta_do_lapis.dto;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;

@JsonTest
@DisplayName("Meta — contrato JSON")
class MetaJsonTest {

    @Autowired
    private JacksonTester<MetaDTO> entrada;

    @Autowired
    private JacksonTester<MetaResponseDTO> saida;

    private static String jsonEntrada(String tipoMeta, String extra) {
        return """
                {
                    "nome": "Reserva de Emergência",
                    "descricao":"Guardar","valor":5000.00,
                    "fotoUrl":"https://x.com/y.png",
                    "dataLimite":"2026-12-31",
                    "tipoMeta":"%s","contaId":1%s
                }
                """
                .formatted(tipoMeta, extra);
    }

    @Test
    @DisplayName("serializa MetaResponseDTO com os nomes e formatos esperados pelo frontend")
    void deveSerializarResposta() throws IOException {
        MetaResponseDTO dto = new MetaResponseDTO(10L, "Reserva", "Desc",
                new BigDecimal("5000.00"), new BigDecimal("1250.00"), "https://x.com/y.png",
                LocalDate.of(2026, 12, 31), TipoMeta.POUPANCA, 7L, 3L, 25.0);

        JsonContent<MetaResponseDTO> json = saida.write(dto);

        assertAll(
                () -> assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(10),
                () -> assertThat(json).extractingJsonPathStringValue("$.nome").isEqualTo("Reserva"),
                () -> assertThat(json).extractingJsonPathNumberValue("$.valorTotal").isEqualTo(5000.0),
                () -> assertThat(json).extractingJsonPathNumberValue("$.valorAtual").isEqualTo(1250.0),
                () -> assertThat(json).extractingJsonPathNumberValue("$.progresso").isEqualTo(25.0),
                () -> assertThat(json).extractingJsonPathStringValue("$.tipoMeta").isEqualTo("POUPANCA"),
                () -> assertThat(json).extractingJsonPathStringValue("$.dataLimite").isEqualTo("2026-12-31"),
                () -> assertThat(json).extractingJsonPathNumberValue("$.usuarioId").isEqualTo(7),
                () -> assertThat(json).extractingJsonPathNumberValue("$.contaId").isEqualTo(3),
                () -> assertThat(json).doesNotHaveJsonPath("$.valor")
        );
    }

    @Test
    @DisplayName("desserializa JSON válido em MetaDTO")
    void deveDesserializarEntrada() throws IOException {
        MetaDTO dto = entrada.parseObject(jsonEntrada("POUPANCA", ""));

        assertAll(
                () -> assertThat(dto.nome()).isEqualTo("Reserva de Emergência"),
                () -> assertThat(dto.descricao()).isEqualTo("Guardar"),
                () -> assertThat(dto.valor()).isEqualByComparingTo("5000.00"),
                () -> assertThat(dto.fotoUrl()).isEqualTo("https://x.com/y.png"),
                () -> assertThat(dto.dataLimite()).isEqualTo(LocalDate.of(2026, 12, 31)),
                () -> assertThat(dto.tipoMeta()).isEqualTo(TipoMeta.POUPANCA),
                () -> assertThat(dto.contaId()).isEqualTo(1L)
        );
    }

    @ParameterizedTest(name = "tipoMeta \"{0}\" é rejeitado pelo Jackson")
    @ValueSource(strings = {"INVESTIMENTO", "poupanca", "Poupança"})
    @DisplayName("tipoMeta só aceita o nome exato da constante (case-sensitive)")
    void deveRejeitarTipoMetaInvalido(String tipo) {
        assertThatThrownBy(() -> entrada.parseObject(jsonEntrada(tipo, "")))
                          .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    @DisplayName("ignora propriedades desconhecidas: o cliente não consegue injetar valorAtual")
    void naoDevePermitirInjetarValorAtual() throws IOException {
        MetaDTO dto = entrada.parseObject(jsonEntrada("POUPANCA", ",\"valorAtual\":999999,\"id\":42"));
        assertThat(dto.toEntity().getValorAtual()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
