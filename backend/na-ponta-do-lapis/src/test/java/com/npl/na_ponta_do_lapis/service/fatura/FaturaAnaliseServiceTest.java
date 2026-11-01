package com.npl.na_ponta_do_lapis.service.fatura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.service.TipoCategoraService;
import com.npl.na_ponta_do_lapis.service.fatura.ia.FaturaIaParser;
import com.npl.na_ponta_do_lapis.service.fatura.ia.FaturaIaService;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import com.npl.na_ponta_do_lapis.web.dto.FaturaAnaliseResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

class FaturaAnaliseServiceTest {

    private FaturaRegexService regexService;
    private FaturaSanitizerService sanitizerService;
    private FaturaOcrService ocrService;
    private FaturaIaService iaService;
    private PadraoFaturaService padraoFaturaService;
    private TipoCategoraService tipoCategoriaService;
    private FaturaAnaliseService service;

    @BeforeEach
    void configurar() {
        regexService = mock(FaturaRegexService.class);
        sanitizerService = new FaturaSanitizerService();
        ocrService = mock(FaturaOcrService.class);
        iaService = mock(FaturaIaService.class);
        padraoFaturaService = mock(PadraoFaturaService.class);
        tipoCategoriaService = mock(TipoCategoraService.class);

        service = new FaturaAnaliseService(regexService, sanitizerService, ocrService,
                iaService, padraoFaturaService, tipoCategoriaService);
        ReflectionTestUtils.setField(service, "minCaracteresTexto", 40);
        ReflectionTestUtils.setField(service, "maxCaracteresIa", 60000);

        when(tipoCategoriaService.listarCategorias()).thenReturn(List.of());
        when(ocrService.habilitado()).thenReturn(false);
    }

    @Test
    void regexComSucessoRetornaFonteRegexENaoChamaIa() throws Exception {
        when(regexService.extrairTexto(any())).thenReturn(textoFatura());
        PadraoFatura padrao = new PadraoFatura();
        padrao.setEmissor("Nubank");
        padrao.setVersao(2);
        when(regexService.analisar(any())).thenReturn(new FaturaRegexService.ResultadoRegex(
                List.of(new TransacaoFaturaDTO("Amazon", new BigDecimal("50.00"),
                        LocalDate.of(2026, 1, 15), "Outros", null, null, null)),
                padrao,
                new BigDecimal("60.00")
        ));

        FaturaAnaliseResponseDTO resposta = service.analisar(arquivo(), null, null, false, false, false);

        assertThat(resposta.fonte()).isEqualTo("REGEX");
        assertThat(resposta.emissor()).isEqualTo("Nubank");
        assertThat(resposta.padraoVersao()).isEqualTo(2);
        assertThat(resposta.transacoes()).hasSize(1);
        assertThat(resposta.somaCalculada()).isEqualByComparingTo("50.00");
        assertThat(resposta.diferenca()).isEqualByComparingTo("10.00");
        assertThat(resposta.alertaSoma()).isTrue();
        verify(iaService, never()).analisarTexto(any(), any(), any(), anyList());
    }

    @Test
    void semRegexESemChaveRetorna428() throws Exception {
        when(regexService.extrairTexto(any())).thenReturn(textoFatura());
        when(regexService.analisar(any())).thenReturn(
                new FaturaRegexService.ResultadoRegex(List.of(), null, null));

        assertThatThrownBy(() -> service.analisar(arquivo(), ProvedorIa.GEMINI, null, false, false, false))
                .isInstanceOfSatisfying(FaturaAnaliseException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(428);
                    assertThat(e.getCodigo()).isEqualTo("IA_NECESSARIA");
                    assertThat(e.getProvedores()).contains("GEMINI", "CLAUDE");
                });

        verify(iaService, never()).analisarTexto(any(), any(), any(), anyList());
    }

    @Test
    void pdfEscaneadoSemConsentimentoPedeAutorizacaoSemChamarIa() throws Exception {
        when(regexService.extrairTexto(any())).thenReturn("curto");

        FaturaAnaliseResponseDTO resposta = service.analisar(arquivo(), null, null, false, false, false);

        assertThat(resposta.precisaConsentimentoPdf()).isTrue();
        assertThat(resposta.pdfEscaneado()).isTrue();
        assertThat(resposta.transacoes()).isEmpty();
        assertThat(resposta.fonte()).isEqualTo("NENHUMA");
        verify(iaService, never()).analisarPdf(any(), any(), any(), anyList());
    }

    @Test
    void envioDePdfSemConsentimentoEhRejeitado() throws Exception {
        when(regexService.extrairTexto(any())).thenReturn("curto");

        assertThatThrownBy(() -> service.analisar(arquivo(), ProvedorIa.GEMINI, "chave", false, true, false))
                .isInstanceOfSatisfying(FaturaAnaliseException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(422);
                    assertThat(e.getCodigo()).isEqualTo("CONSENTIMENTO_OBRIGATORIO");
                });
    }

    @Test
    void provedorSemSuportePdfEhRejeitado() throws Exception {
        when(regexService.extrairTexto(any())).thenReturn("curto");

        assertThatThrownBy(() -> service.analisar(arquivo(), ProvedorIa.OPENAI, "chave", false, true, true))
                .isInstanceOfSatisfying(FaturaAnaliseException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(422);
                    assertThat(e.getCodigo()).isEqualTo("PROVEDOR_SEM_SUPORTE_PDF");
                });
    }

    @Test
    void iaDeTextoSanitizaAntesDeEnviarEPersistePadraoValidado() throws Exception {
        when(regexService.extrairTexto(any())).thenReturn(textoFatura());
        when(regexService.analisar(any())).thenReturn(
                new FaturaRegexService.ResultadoRegex(List.of(), null, null));

        PadraoFatura padrao = new PadraoFatura();
        padrao.setEmissor("Nubank");
        padrao.setVersao(3);
        FaturaIaParser.ResultadoIa resultadoIa = new FaturaIaParser.ResultadoIa(
                List.of(new TransacaoFaturaDTO("Loja", new BigDecimal("10.00"),
                        LocalDate.of(2026, 1, 15), "Outros", null, null, null)),
                padrao,
                new BigDecimal("10.00")
        );
        when(iaService.analisarTexto(eq(ProvedorIa.GEMINI), eq("chave"), any(), anyList()))
                .thenReturn(resultadoIa);
        when(padraoFaturaService.salvarNovaVersao(padrao)).thenReturn(padrao);

        FaturaAnaliseResponseDTO resposta = service.analisar(arquivo(), ProvedorIa.GEMINI, "chave", false, false, false);

        assertThat(resposta.fonte()).isEqualTo("IA");
        assertThat(resposta.emissor()).isEqualTo("Nubank");
        assertThat(resposta.padraoVersao()).isEqualTo(3);
        assertThat(resposta.alertaSoma()).isFalse();
        verify(iaService).analisarTexto(eq(ProvedorIa.GEMINI), eq("chave"), any(), anyList());
        verify(padraoFaturaService).salvarNovaVersao(padrao);
    }

    @Test
    void arquivoNaoPdfEhRejeitado() {
        MockMultipartFile txt = new MockMultipartFile("arquivo", "fatura.txt", "text/plain",
                "conteudo".getBytes());

        assertThatThrownBy(() -> service.analisar(txt, null, null, false, false, false))
                .isInstanceOfSatisfying(FaturaAnaliseException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(400);
                    assertThat(e.getCodigo()).isEqualTo("ARQUIVO_INVALIDO");
                });
    }

    private MockMultipartFile arquivo() {
        return new MockMultipartFile("arquivo", "fatura.pdf", "application/pdf",
                "%PDF-1.4 conteudo".getBytes());
    }

    private String textoFatura() {
        return "FATURA NUBANK\n15/01/2026 AMAZON 50,00\nTotal da fatura R$ 60,00\n";
    }
}
