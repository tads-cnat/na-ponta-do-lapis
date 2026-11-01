package com.npl.na_ponta_do_lapis.service.fatura.ia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.repository.PadraoFaturaRepository;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaRegexService;

class FaturaIaParserTest {

    private FaturaIaParser parser;

    @BeforeEach
    void configurar() {
        FaturaRegexService regexService = new FaturaRegexService(mock(PadraoFaturaRepository.class));
        ReflectionTestUtils.setField(regexService, "timeoutMs", 5000L);
        ReflectionTestUtils.setField(regexService, "maxTextChars", 300000);
        parser = new FaturaIaParser(new ObjectMapper(), regexService);
    }

    @Test
    void interpretaJsonComCercasMarkdownEValoresBrasileiros() {
        String resposta = """
                ```json
                {
                  "valorTotalFatura": "R$ 1.234,56",
                  "transacoes": [
                    {
                      "descricao": "  Amazon  ",
                      "valor": "R$ 50,00",
                      "data": "2026-01-15",
                      "categoria": "Outros",
                      "numeroParcela": 2,
                      "totalParcelas": 10,
                      "valorTotalCompra": "R$ 500,00"
                    }
                  ]
                }
                ```
                """;

        FaturaIaParser.ResultadoIa resultado = parser.parse(resposta, "texto", List.of("Outros", "Alimentação"));

        assertThat(resultado.transacoes()).hasSize(1);
        assertThat(resultado.transacoes().get(0).descricao()).isEqualTo("Amazon");
        assertThat(resultado.transacoes().get(0).valor()).isEqualByComparingTo("50.00");
        assertThat(resultado.transacoes().get(0).data()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(resultado.transacoes().get(0).numeroParcela()).isEqualTo(2);
        assertThat(resultado.transacoes().get(0).totalParcelas()).isEqualTo(10);
        assertThat(resultado.transacoes().get(0).valorTotalCompra()).isEqualByComparingTo("500.00");
        assertThat(resultado.valorTotalFatura()).isEqualByComparingTo("1234.56");
    }

    @Test
    void normalizaParcelamentoInvalidoEAceitaCategoriaComAcento() {
        String resposta = """
                {
                  "transacoes": [
                    {
                      "descricao": "Padaria",
                      "valor": 12.5,
                      "data": "15/01/2026",
                      "categoria": "alimentacao",
                      "numeroParcela": 12,
                      "totalParcelas": 10
                    }
                  ]
                }
                """;

        FaturaIaParser.ResultadoIa resultado = parser.parse(resposta, "texto",
                List.of("Alimentação", "Outros"));

        assertThat(resultado.transacoes().get(0).numeroParcela()).isNull();
        assertThat(resultado.transacoes().get(0).totalParcelas()).isNull();
        assertThat(resultado.transacoes().get(0).categoria()).isEqualTo("Alimentação");
        assertThat(resultado.valorTotalFatura()).isNull();
    }

    @Test
    void rejeitaRespostaSemTransacoesValidas() {
        String resposta = """
                {"transacoes": [{"descricao": "", "valor": 10, "data": "2026-01-15"}]}
                """;

        assertThatThrownBy(() -> parser.parse(resposta, "texto", List.of("Outros")))
                .isInstanceOf(FaturaAnaliseException.class)
                .hasMessageContaining("Nenhuma transação válida");
    }

    @Test
    void rejeitaJsonInvalido() {
        assertThatThrownBy(() -> parser.parse("isto não é json", "texto", List.of("Outros")))
                .isInstanceOf(FaturaAnaliseException.class);
    }

    @Test
    void padraoSugeridoQueNaoExtraiNadaEhDescartado() {
        String resposta = """
                {
                  "padrao": {
                    "emissor": "Banco X",
                    "regexIdentificacao": "banco x",
                    "regexTransacao": "(?<descricao>.*)",
                    "regexTotal": "(\\\\d+,\\\\d{2})",
                    "formatoData": "dd/MM/yyyy"
                  },
                  "transacoes": [
                    {"descricao": "Loja", "valor": 10, "data": "2026-01-15", "categoria": "Outros"}
                  ]
                }
                """;

        FaturaIaParser.ResultadoIa resultado = parser.parse(resposta, "texto sem o padrao", List.of("Outros"));

        assertThat(resultado.padrao()).isNull();
        assertThat(resultado.transacoes()).hasSize(1);
    }
}
