package com.npl.na_ponta_do_lapis.service.fatura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.PadraoFaturaRepository;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

/**
 * Regressão com uma fatura real do Nubank em que o PDF entrega datas,
 * descrições e valores embaralhados. A soma das transações precisa fechar
 * exatamente com o "Total a pagar" (despesas − receitas).
 */
class FaturaNubankExemploTest {

    private FaturaRegexService service;

    @BeforeEach
    void configurar() {
        PadraoFaturaRepository repository = mock(PadraoFaturaRepository.class);
        service = new FaturaRegexService(repository);
        ReflectionTestUtils.setField(service, "timeoutMs", 5000L);
        ReflectionTestUtils.setField(service, "maxTextChars", 300000);

        PadraoFatura padrao = new PadraoFatura();
        padrao.setEmissor("Nubank");
        padrao.setRegexIdentificacao("(?i)nubank");
        padrao.setRegexTransacao(
                "(?m)^\\s*(?<data>\\d{2}/\\d{2}/\\d{4})\\s+"
                        + "(?<descricao>.+?)\\s+(?<valor>-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*$");
        padrao.setRegexTotal("(?i)total\\s+a\\s+pagar\\s*:?\\s*(?:R\\$\\s*)?([\\d.]+,\\d{2})");
        padrao.setFormatoData("dd/MM/yyyy");
        padrao.setVersao(3);
        padrao.setAtivo(true);
        when(repository.findByAtivoTrueOrderByVersaoDesc()).thenReturn(List.of(padrao));
    }

    @Test
    void somaDasTransacoesBateComOTotalDaFatura() throws Exception {
        String texto = Files.readString(
                Path.of("src/test/resources/fatura/nubank-exemplo.txt"), StandardCharsets.UTF_8);

        FaturaRegexService.ResultadoRegex resultado = service.analisar(texto);

        assertThat(resultado.valorTotalFatura()).isEqualByComparingTo("3471.23");
        assertThat(resultado.transacoes()).hasSizeGreaterThan(50);

        BigDecimal soma = resultado.transacoes().stream()
                .map(transacao -> transacao.tipo() == TipoTransacao.RECEITA
                        ? transacao.valor().negate()
                        : transacao.valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(soma).isEqualByComparingTo("3471.23");
    }

    @Test
    void leFaturaMesmoSemPadraoAtivo() throws Exception {
        PadraoFaturaRepository semPadroes = mock(PadraoFaturaRepository.class);
        when(semPadroes.findByAtivoTrueOrderByVersaoDesc()).thenReturn(List.of());
        FaturaRegexService local = new FaturaRegexService(semPadroes);
        ReflectionTestUtils.setField(local, "timeoutMs", 5000L);
        ReflectionTestUtils.setField(local, "maxTextChars", 300000);

        String texto = Files.readString(
                Path.of("src/test/resources/fatura/nubank-exemplo.txt"), StandardCharsets.UTF_8);

        FaturaRegexService.ResultadoRegex resultado = local.analisar(texto);

        assertThat(resultado.padrao()).isNull();
        assertThat(resultado.valorTotalFatura()).isEqualByComparingTo("3471.23");
        BigDecimal soma = resultado.transacoes().stream()
                .map(transacao -> transacao.tipo() == TipoTransacao.RECEITA
                        ? transacao.valor().negate()
                        : transacao.valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(soma).isEqualByComparingTo("3471.23");
    }

    @Test
    void preservaComprasEPagamentoDoResumo() throws Exception {
        String texto = Files.readString(
                Path.of("src/test/resources/fatura/nubank-exemplo.txt"), StandardCharsets.UTF_8);

        FaturaRegexService.ResultadoRegex resultado = service.analisar(texto);

        assertThat(resultado.transacoes())
                .anyMatch(t -> t.descricao().contains("Assai Atacadista")
                        && t.valor().compareTo(new BigDecimal("695.50")) == 0
                        && t.tipo() == TipoTransacao.DESPESA);

        assertThat(resultado.transacoes())
                .anyMatch(t -> t.descricao().equals("Pagamento recebido")
                        && t.valor().compareTo(new BigDecimal("3076.07")) == 0
                        && t.tipo() == TipoTransacao.RECEITA);

        assertThat(resultado.transacoes())
                .anyMatch(t -> t.descricao().equals("Fatura anterior")
                        && t.valor().compareTo(new BigDecimal("2381.07")) == 0
                        && t.tipo() == TipoTransacao.DESPESA);

        assertThat(resultado.transacoes())
                .noneMatch(t -> t.descricao().contains("••••"));
    }
}
