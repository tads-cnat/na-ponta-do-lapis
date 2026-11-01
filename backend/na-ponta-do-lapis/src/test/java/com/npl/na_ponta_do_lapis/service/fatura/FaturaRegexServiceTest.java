package com.npl.na_ponta_do_lapis.service.fatura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.repository.PadraoFaturaRepository;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

class FaturaRegexServiceTest {

    private PadraoFaturaRepository repository;
    private FaturaRegexService service;

    @BeforeEach
    void configurar() {
        repository = mock(PadraoFaturaRepository.class);
        service = new FaturaRegexService(repository);
        ReflectionTestUtils.setField(service, "timeoutMs", 5000L);
        ReflectionTestUtils.setField(service, "maxTextChars", 300000);
    }

    @Test
    void converteValoresBrasileirosENorteAmericanos() {
        assertThat(FaturaRegexService.parseValor("R$ 1.234,56")).isEqualByComparingTo("1234.56");
        assertThat(FaturaRegexService.parseValor("1,234.56")).isEqualByComparingTo("1234.56");
        assertThat(FaturaRegexService.parseValor("45,90")).isEqualByComparingTo("45.90");
        assertThat(FaturaRegexService.parseValor("45.90")).isEqualByComparingTo("45.90");
        assertThat(FaturaRegexService.parseValor("-10,00")).isEqualByComparingTo("-10.00");
        assertThat(FaturaRegexService.parseValor("1.234")).isEqualByComparingTo("1234");
        assertThat(FaturaRegexService.parseValor("")).isNull();
        assertThat(FaturaRegexService.parseValor("abc")).isNull();
    }

    @Test
    void converteFormatosDeDataConhecidos() {
        assertThat(service.parseData("15/01/2026", null)).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(service.parseData("2026-01-15", null)).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(service.parseData("15 JAN 2026", null)).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(service.parseData("15 de janeiro de 2026", null)).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(service.parseData("31/02/2026", null)).isNull();
        assertThat(service.parseData("", null)).isNull();
    }

    @Test
    void classificaCategoriasPorPalavraChave() {
        assertThat(service.classificarCategoria("UBER TRIP")).isEqualTo("Transporte");
        assertThat(service.classificarCategoria("SUPERMERCADO EXTRA")).isEqualTo("Alimentação");
        assertThat(service.classificarCategoria("FARMACIA CENTRAL")).isEqualTo("Saúde");
        assertThat(service.classificarCategoria("NETFLIX.COM")).isEqualTo("Lazer");
        assertThat(service.classificarCategoria("CURSO ALURA")).isEqualTo("Educação");
        assertThat(service.classificarCategoria("LOJA DESCONHECIDA")).isEqualTo("Outros");
    }

    @Test
    void extraiTransacoesParcelasETotal() {
        PadraoFatura padrao = padrao();
        when(repository.findByAtivoTrueOrderByVersaoDesc()).thenReturn(List.of(padrao));

        String texto = """
                FATURA NUBANK
                15/01/2026 AMAZON 50,00
                16/01/2026 SUPERMERCADO EXTRA 120,50
                17/01/2026 NETFLIX (02/10) 19,90
                Pagamento de fatura 100,00
                Total da fatura R$ 190,40
                """;

        FaturaRegexService.ResultadoRegex resultado = service.analisar(texto);

        assertThat(resultado.padrao()).isSameAs(padrao);
        assertThat(resultado.transacoes()).hasSize(3);

        TransacaoFaturaDTO amazon = resultado.transacoes().get(0);
        assertThat(amazon.descricao()).isEqualTo("AMAZON");
        assertThat(amazon.valor()).isEqualByComparingTo("50.00");
        assertThat(amazon.data()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(amazon.categoria()).isEqualTo("Outros");
        assertThat(amazon.numeroParcela()).isNull();

        TransacaoFaturaDTO netflix = resultado.transacoes().get(2);
        assertThat(netflix.descricao()).isEqualTo("NETFLIX");
        assertThat(netflix.numeroParcela()).isEqualTo(2);
        assertThat(netflix.totalParcelas()).isEqualTo(10);
        assertThat(netflix.categoria()).isEqualTo("Lazer");

        assertThat(resultado.valorTotalFatura()).isEqualByComparingTo("190.40");
    }

    @Test
    void padraoSemTransacoesValidasNaoEhUsado() {
        PadraoFatura padrao = padrao();
        when(repository.findByAtivoTrueOrderByVersaoDesc()).thenReturn(List.of(padrao));

        FaturaRegexService.ResultadoRegex resultado = service.analisar("FATURA NUBANK sem linhas de compra");

        assertThat(resultado.transacoes()).isEmpty();
        assertThat(resultado.padrao()).isNull();
    }

    @Test
    void validaPadraoQueRealmenteExtraiTransacao() {
        PadraoFatura padrao = padrao();
        String texto = "FATURA NUBANK\n15/01/2026 AMAZON 50,00";

        assertThat(service.validarPadrao(padrao, texto)).isTrue();

        PadraoFatura invalido = padrao();
        invalido.setRegexTransacao("(?<descricao>.*)");
        assertThat(service.validarPadrao(invalido, texto)).isFalse();
    }

    private PadraoFatura padrao() {
        PadraoFatura padrao = new PadraoFatura();
        padrao.setEmissor("Nubank");
        padrao.setRegexIdentificacao("(?i)nubank");
        padrao.setRegexTransacao(
                "(?m)^\\s*(?<data>\\d{2}/\\d{2}/\\d{4})\\s+"
                        + "(?<descricao>.+?)\\s+(?<valor>-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*$");
        padrao.setRegexTotal("(?i)total\\s+da\\s+fatura\\s+R\\$\\s*([\\d.]+,\\d{2})");
        padrao.setFormatoData("dd/MM/yyyy");
        padrao.setVersao(1);
        padrao.setAtivo(true);
        return padrao;
    }
}
