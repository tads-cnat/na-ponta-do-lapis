package com.npl.na_ponta_do_lapis.service.fatura;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
    private final TipoCategoraService tipoCategoriaService;

    @Value("${fatura.ocr.min-chars:40}")
    private int minCaracteresTexto;

    @Value("${fatura.ia.max-text-chars:60000}")
    private int maxCaracteresIa;

    public FaturaAnaliseService(FaturaRegexService regexService,
                                FaturaSanitizerService sanitizerService,
                                FaturaOcrService ocrService,
                                FaturaIaService iaService,
                                TipoCategoraService tipoCategoriaService) {
        this.regexService = regexService;
        this.sanitizerService = sanitizerService;
        this.ocrService = ocrService;
        this.iaService = iaService;
        this.tipoCategoriaService = tipoCategoriaService;
    }

    public FaturaAnaliseResponseDTO analisar(MultipartFile arquivo,
                                             ProvedorIa provedor,
                                             String apiKey,
                                             boolean usarIa,
                                             boolean enviarPdfParaIa,
                                             boolean consentimentoLgpd) {
        return analisar(arquivo, provedor, apiKey, null, usarIa, enviarPdfParaIa, consentimentoLgpd);
    }

    public FaturaAnaliseResponseDTO analisar(MultipartFile arquivo,
                                             ProvedorIa provedor,
                                             String apiKey,
                                             String senhaPdf,
                                             boolean usarIa,
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
            texto = regexService.extrairTexto(bytes, senhaPdf);
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
            if (senhaPdf == null || senhaPdf.isBlank()) {
                throw FaturaAnaliseException.pdfProtegidoPorSenha();
            } else {
                throw FaturaAnaliseException.senhaIncorreta();
            }
        } catch (IOException e) {
            throw FaturaAnaliseException.arquivoInvalido("O arquivo não é um PDF válido ou está corrompido.");
        }

        boolean ocrUtilizado = false;
        boolean pdfEscaneado = texto.length() < minCaracteresTexto;

        if (pdfEscaneado && ocrService.habilitado()) {
            String textoOcr = ocrService.extrairTexto(bytes, senhaPdf);
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
                    true, ocrUtilizado, false, true, false, false,
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

            log.info("Envio multimodal de PDF autorizado pelo usuário (provedor={}).", provedor);
            FaturaIaParser.ResultadoIa resultado = iaService.analisarPdf(provedor, apiKey, bytes, categorias());
            return montarResposta(resultado.transacoes(), resultado.valorTotalFatura(), resultado.emissor(),
                    pdfEscaneado, ocrUtilizado, true, null);
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw FaturaAnaliseException.precisaIa();
        }

        String textoSanitizado = sanitizerService.sanitizar(texto);
        if (textoSanitizado.length() > maxCaracteresIa) {
            textoSanitizado = textoSanitizado.substring(0, maxCaracteresIa);
        }

        FaturaIaParser.ResultadoIa resultadoIa = iaService.analisarTexto(
                provedor, apiKey, textoSanitizado, categorias(), FaturaIaService.ContextoFaturaIa.vazio());

        String emissor = resultadoIa.emissor();
        if (emissor == null || emissor.isBlank()) {
            emissor = identificarEmissor(texto);
        }

        BigDecimal total = resultadoIa.valorTotalFatura() != null
                ? resultadoIa.valorTotalFatura()
                : regexService.extrairTotal(texto);

        return montarResposta(resultadoIa.transacoes(), total, emissor,
                false, ocrUtilizado, false, null);
    }

    private String identificarEmissor(String texto) {
        if (texto == null) return null;
        String lower = texto.toLowerCase();
        if (lower.contains("santander")) return "Santander";
        if (lower.contains("nubank") || lower.contains("nu pagamentos")) return "Nubank";
        if (lower.contains("banco inter") || lower.contains("inter dtvm") || lower.contains("inter s.a.")) return "Inter";
        if (lower.contains("itau") || lower.contains("itaú")) return "Itaú";
        if (lower.contains("bradesco")) return "Bradesco";
        if (lower.contains("c6 bank") || lower.contains("c6bank")) return "C6 Bank";
        if (lower.contains("banco do brasil")) return "Banco do Brasil";
        if (lower.contains("caixa economica") || lower.contains("caixa econômica")) return "Caixa";
        return null;
    }

    private BigDecimal somar(List<TransacaoFaturaDTO> transacoes) {
        return FaturaRegexService.somar(transacoes);
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
                                                    String emissor,
                                                    boolean pdfEscaneado,
                                                    boolean ocrUtilizado,
                                                    boolean consentimentoPdfIa,
                                                    String mensagem) {
        BigDecimal soma = somar(transacoes);
        BigDecimal diferenca = valorTotalFatura == null ? null : valorTotalFatura.subtract(soma);
        boolean alertaSoma = diferenca != null && diferenca.abs().compareTo(UM_CENTAVO) > 0;

        return new FaturaAnaliseResponseDTO(
                transacoes,
                "IA",
                emissor,
                soma,
                valorTotalFatura,
                diferenca,
                alertaSoma,
                null,
                pdfEscaneado,
                ocrUtilizado,
                consentimentoPdfIa,
                false,
                false,
                false,
                mensagem
        );
    }
}
