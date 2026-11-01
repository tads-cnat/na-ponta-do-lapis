package com.npl.na_ponta_do_lapis.service.fatura;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.service.TipoCategoraService;
import com.npl.na_ponta_do_lapis.service.fatura.ia.FaturaIaParser;
import com.npl.na_ponta_do_lapis.service.fatura.ia.FaturaIaService;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import com.npl.na_ponta_do_lapis.web.dto.FaturaAnaliseResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

/**
 * Orquestra a análise: texto local -> regex -> OCR -> consentimento -> IA.
 */
@Service
public class FaturaAnaliseService {

    private static final Logger log = LoggerFactory.getLogger(FaturaAnaliseService.class);
    private static final BigDecimal UM_CENTAVO = new BigDecimal("0.01");

    private final FaturaRegexService regexService;
    private final FaturaSanitizerService sanitizerService;
    private final FaturaOcrService ocrService;
    private final FaturaIaService iaService;
    private final PadraoFaturaService padraoFaturaService;
    private final TipoCategoraService tipoCategoriaService;

    @Value("${fatura.ocr.min-chars:40}")
    private int minCaracteresTexto;

    @Value("${fatura.ia.max-text-chars:60000}")
    private int maxCaracteresIa;

    public FaturaAnaliseService(FaturaRegexService regexService,
                                FaturaSanitizerService sanitizerService,
                                FaturaOcrService ocrService,
                                FaturaIaService iaService,
                                PadraoFaturaService padraoFaturaService,
                                TipoCategoraService tipoCategoriaService) {
        this.regexService = regexService;
        this.sanitizerService = sanitizerService;
        this.ocrService = ocrService;
        this.iaService = iaService;
        this.padraoFaturaService = padraoFaturaService;
        this.tipoCategoriaService = tipoCategoriaService;
    }

    public FaturaAnaliseResponseDTO analisar(MultipartFile arquivo,
                                             ProvedorIa provedor,
                                             String apiKey,
                                             boolean forceAiAnalysis,
                                             boolean enviarPdfParaIa,
                                             boolean consentimentoLgpd) {
        validarArquivo(arquivo);

        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw FaturaAnaliseException.arquivoInvalido("Não foi possível ler o arquivo enviado.");
        }

        String texto;
        try {
            texto = regexService.extrairTexto(bytes);
        } catch (IOException e) {
            throw FaturaAnaliseException.arquivoInvalido("O arquivo não é um PDF válido ou está corrompido.");
        }

        boolean ocrUtilizado = false;
        boolean pdfEscaneado = texto.length() < minCaracteresTexto;

        if (pdfEscaneado && ocrService.habilitado()) {
            String textoOcr = ocrService.extrairTexto(bytes);
            if (textoOcr.length() > texto.length()) {
                texto = textoOcr;
                ocrUtilizado = true;
                pdfEscaneado = texto.length() < minCaracteresTexto;
            }
        }

        if (pdfEscaneado && !enviarPdfParaIa) {
            return new FaturaAnaliseResponseDTO(
                    List.of(), "NENHUMA", null,
                    BigDecimal.ZERO, null, null, false, null,
                    true, ocrUtilizado, false, true,
                    "Não foi possível ler o texto deste PDF. Ele parece ser escaneado. "
                            + "Você pode autorizar o envio do PDF a um provedor de IA multimodal."
            );
        }

        if (enviarPdfParaIa) {
            if (!consentimentoLgpd) {
                throw FaturaAnaliseException.consentimentoObrigatorio();
            }
            if (provedor == null || !provedor.isSuportaPdf()) {
                throw FaturaAnaliseException.provedorSemSuportePdf();
            }
            if (apiKey == null || apiKey.isBlank()) {
                throw FaturaAnaliseException.precisaIa();
            }

            log.info("Envio multimodal de PDF autorizado explicitamente pelo usuário (provedor={}).", provedor);
            FaturaIaParser.ResultadoIa resultado = iaService.analisarPdf(provedor, apiKey, bytes, categorias());
            return montarResposta(resultado.transacoes(), resultado.valorTotalFatura(), "IA",
                    null, pdfEscaneado, ocrUtilizado, true, null);
        }

        if (!forceAiAnalysis) {
            FaturaRegexService.ResultadoRegex resultadoRegex = regexService.analisar(texto);
            if (!resultadoRegex.transacoes().isEmpty()) {
                return montarResposta(resultadoRegex.transacoes(), resultadoRegex.valorTotalFatura(), "REGEX",
                        resultadoRegex.padrao(), false, ocrUtilizado, false, null);
            }
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw FaturaAnaliseException.precisaIa();
        }

        String textoSanitizado = sanitizerService.sanitizar(texto);
        if (textoSanitizado.length() > maxCaracteresIa) {
            textoSanitizado = textoSanitizado.substring(0, maxCaracteresIa);
        }

        FaturaIaParser.ResultadoIa resultadoIa = iaService.analisarTexto(
                provedor, apiKey, textoSanitizado, categorias());

        PadraoFatura padraoSalvo = null;
        if (resultadoIa.padrao() != null) {
            padraoSalvo = padraoFaturaService.salvarNovaVersao(resultadoIa.padrao());
        }

        return montarResposta(resultadoIa.transacoes(), resultadoIa.valorTotalFatura(), "IA",
                padraoSalvo, false, ocrUtilizado, false, null);
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw FaturaAnaliseException.arquivoInvalido("Arquivo vazio. Envie um PDF de fatura.");
        }
        String nome = arquivo.getOriginalFilename();
        String tipo = arquivo.getContentType();
        boolean pdfPorNome = nome != null && nome.toLowerCase().endsWith(".pdf");
        boolean pdfPorTipo = tipo != null && tipo.equalsIgnoreCase("application/pdf");
        if (!pdfPorNome && !pdfPorTipo) {
            throw FaturaAnaliseException.arquivoInvalido("Formato não suportado. Envie um arquivo PDF.");
        }
    }

    private List<String> categorias() {
        return tipoCategoriaService.listarCategorias().stream()
                .map(categoria -> categoria.nome())
                .toList();
    }

    private FaturaAnaliseResponseDTO montarResposta(List<TransacaoFaturaDTO> transacoes,
                                                    BigDecimal valorTotalFatura,
                                                    String fonte,
                                                    PadraoFatura padrao,
                                                    boolean pdfEscaneado,
                                                    boolean ocrUtilizado,
                                                    boolean consentimentoPdfIa,
                                                    String mensagem) {
        BigDecimal soma = transacoes.stream()
                .map(TransacaoFaturaDTO::valor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal diferenca = valorTotalFatura == null ? null : valorTotalFatura.subtract(soma);
        boolean alertaSoma = diferenca != null && diferenca.abs().compareTo(UM_CENTAVO) > 0;

        return new FaturaAnaliseResponseDTO(
                transacoes,
                fonte,
                padrao == null ? null : padrao.getEmissor(),
                soma,
                valorTotalFatura,
                diferenca,
                alertaSoma,
                padrao == null ? null : padrao.getVersao(),
                pdfEscaneado,
                ocrUtilizado,
                consentimentoPdfIa,
                false,
                mensagem
        );
    }
}
