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

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
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
            if (!usarIa) {
                return respostaPrecisaHabilitarIa(true, ocrUtilizado,
                        "Não foi possível ler esta fatura sem usar IA. "
                                + "Ative o uso de IA e informe uma chave de acesso para continuar.");
            }
            return new FaturaAnaliseResponseDTO(
                    List.of(), "NENHUMA", null,
                    BigDecimal.ZERO, null, null, false, null,
                    true, ocrUtilizado, false, true, false,
                    "Não foi possível ler o texto deste PDF. Ele parece ser escaneado. "
                            + "Você pode autorizar o envio do PDF a um provedor de IA multimodal."
            );
        }

        if (enviarPdfParaIa) {
            if (!usarIa) {
                return respostaPrecisaHabilitarIa(pdfEscaneado, ocrUtilizado,
                        "O envio do PDF para a IA está desativado. "
                                + "Ative o uso de IA e informe uma chave de acesso para continuar.");
            }
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
            ResultadoAnalise avaliado = avaliarResultadoIa(texto, resultado);
            return montarResposta(avaliado.transacoes(), avaliado.valorTotalFatura(), "IA",
                    null, pdfEscaneado, ocrUtilizado, true, null);
        }

        // 1. Local-First: se o leitor local conseguir ler e a conta fechar 100%,
        // dispensamos a chamada à IA para economizar tokens do usuário e responder instantaneamente.
        FaturaRegexService.ResultadoRegex resultadoRegex = regexService.analisar(texto);
        if (resultadoRegex != null && !resultadoRegex.transacoes().isEmpty() && resultadoBateComTotal(resultadoRegex)) {
            log.info("Fatura resolvida com sucesso pelo leitor local. Chamada à IA dispensada para economizar tokens.");
            return montarResposta(resultadoRegex.transacoes(), resultadoRegex.valorTotalFatura(), "REGEX",
                    resultadoRegex.padrao(), false, ocrUtilizado, false, null);
        }

        if (!usarIa) {
            if (resultadoRegex != null && !resultadoRegex.transacoes().isEmpty()) {
                return montarResposta(resultadoRegex.transacoes(), resultadoRegex.valorTotalFatura(), "REGEX",
                        resultadoRegex.padrao(), false, ocrUtilizado, false, null);
            }
            return respostaPrecisaHabilitarIa(pdfEscaneado, ocrUtilizado,
                    "Não conseguimos ler esta fatura automaticamente. "
                            + "Ative o uso de IA e informe uma chave de acesso para continuar.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw FaturaAnaliseException.precisaIa();
        }

        String textoSanitizado = sanitizerService.sanitizar(texto);
        if (textoSanitizado.length() > maxCaracteresIa) {
            textoSanitizado = textoSanitizado.substring(0, maxCaracteresIa);
        }

        FaturaIaParser.ResultadoIa resultadoIa = iaService.analisarTexto(
                provedor, apiKey, textoSanitizado, categorias(), contextoIa(texto));

        ResultadoAnalise avaliado = avaliarResultadoIa(texto, resultadoIa);

        PadraoFatura padraoSalvo = null;
        PadraoFatura padraoCandidato = resultadoIa.padrao();

        if (!avaliado.usouFallbackLocal()
                && padraoCandidato != null
                && !regexService.possuiLeituraDeResumoConfiavel(texto)) {

            List<TransacaoFaturaDTO> transacoesPadrao = regexService.aplicarPadrao(padraoCandidato, texto);
            BigDecimal totalPadrao = regexService.extrairTotal(padraoCandidato, texto);
            BigDecimal totalEsperado = totalPadrao != null ? totalPadrao : avaliado.valorTotalFatura();

            boolean fechaExato = totalEsperado != null && !transacoesPadrao.isEmpty()
                    && somar(transacoesPadrao).subtract(totalEsperado).abs().compareTo(UM_CENTAVO) <= 0;

            if (!fechaExato && apiKey != null && !apiKey.isBlank()) {
                try {
                    log.info("Padrão inicial da IA divergiu do total (soma={}, total={}). Disparando auto-refinamento...",
                            somar(transacoesPadrao), totalEsperado);
                    PadraoFatura padraoRefinado = iaService.refinarPadrao(provedor, apiKey, padraoCandidato,
                            textoSanitizado, totalEsperado, transacoesPadrao);
                    if (padraoRefinado != null) {
                        List<TransacaoFaturaDTO> transacoesRefinadas = regexService.aplicarPadrao(padraoRefinado, texto);
                        BigDecimal totalRefinado = regexService.extrairTotal(padraoRefinado, texto);
                        BigDecimal totalRefEfetivo = totalRefinado != null ? totalRefinado : totalEsperado;

                        boolean refinadoFecha = totalRefEfetivo != null && !transacoesRefinadas.isEmpty()
                                && somar(transacoesRefinadas).subtract(totalRefEfetivo).abs().compareTo(UM_CENTAVO) <= 0;

                        if (refinadoFecha || transacoesRefinadas.size() > transacoesPadrao.size()) {
                            log.info("Padrão da IA refinado com sucesso no segundo turno!");
                            padraoCandidato = padraoRefinado;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Auto-refinamento da IA não pôde ser concluído: {}", e.getMessage());
                }
            }

            if (regexService.validarPadrao(padraoCandidato, texto)
                    && !regexService.padraoPiorQueAtivo(padraoCandidato, texto, avaliado.transacoes().size())) {
                padraoSalvo = padraoFaturaService.salvarNovaVersao(padraoCandidato);
            } else {
                log.info("Padrão sugerido pela IA não foi salvo: resumo local confiável, "
                        + "fallback local ou qualidade inferior ao padrão ativo.");
            }
        }

        PadraoFatura padraoEfetivo = padraoSalvo != null ? padraoSalvo : (padraoCandidato != null ? padraoCandidato : avaliado.padrao());
        return montarResposta(avaliado.transacoes(), avaliado.valorTotalFatura(),
                avaliado.usouFallbackLocal() ? "REGEX" : "IA",
                padraoEfetivo, false, ocrUtilizado, false, null);
    }

    private FaturaIaService.ContextoFaturaIa contextoIa(String texto) {
        FaturaRegexService.ResumoFatura resumo = regexService.extrairResumoFatura(texto);
        if (resumo == null) {
            return FaturaIaService.ContextoFaturaIa.vazio();
        }
        return new FaturaIaService.ContextoFaturaIa(
                resumo.totalPagar(),
                resumo.faturaAnterior(),
                resumo.pagamentoRecebido(),
                resumo.totalCompras(),
                resumo.iofInternacional(),
                resumo.outrosLancamentos()
        );
    }

    /**
     * Confere o resultado da IA contra o total detectado localmente. Se a IA
     * deixar um resíduo grande na conciliação e a leitura local fechar exata, a
     * leitura local é preferida para não importar valores errados.
     */
    private ResultadoAnalise avaliarResultadoIa(String texto, FaturaIaParser.ResultadoIa resultadoIa) {
        BigDecimal totalLocal = regexService.extrairTotal(null, texto);
        BigDecimal total = totalLocal != null ? totalLocal : resultadoIa.valorTotalFatura();
        List<TransacaoFaturaDTO> transacoes = new ArrayList<>(resultadoIa.transacoes());

        if (total != null) {
            List<TransacaoFaturaDTO> conciliadas = regexService.conciliarComResumo(texto, transacoes, total);
            BigDecimal ajuste = ajusteDeConciliacao(conciliadas);
            if (ajuste.compareTo(limiteDeAjuste(total)) > 0) {
                FaturaRegexService.ResultadoRegex local = regexService.analisar(texto);
                if (resultadoBateComTotal(local)) {
                    log.warn("Resultado da IA descartado: ajuste de conciliação de R$ {} acima do limite. "
                            + "Usando a leitura local, que fecha com o total.", ajuste.toPlainString());
                    return new ResultadoAnalise(local.transacoes(), local.valorTotalFatura(), local.padrao(), true);
                }
            }
            transacoes = conciliadas;
        }

        return new ResultadoAnalise(transacoes, total, resultadoIa.padrao(), false);
    }

    private BigDecimal ajusteDeConciliacao(List<TransacaoFaturaDTO> transacoes) {
        return transacoes.stream()
                .filter(transacao -> transacao.descricao() != null
                        && transacao.descricao().startsWith("Ajuste de conciliação"))
                .map(TransacaoFaturaDTO::valor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal limiteDeAjuste(BigDecimal total) {
        BigDecimal doisPorCento = total.abs().multiply(new BigDecimal("0.02"));
        return doisPorCento.max(new BigDecimal("5.00"));
    }

    private boolean resultadoBateComTotal(FaturaRegexService.ResultadoRegex resultado) {
        if (resultado == null || resultado.transacoes().isEmpty() || resultado.valorTotalFatura() == null) {
            return false;
        }
        BigDecimal soma = somar(resultado.transacoes());
        return soma.subtract(resultado.valorTotalFatura()).abs().compareTo(UM_CENTAVO) <= 0;
    }

    private BigDecimal somar(List<TransacaoFaturaDTO> transacoes) {
        return FaturaRegexService.somar(transacoes);
    }

    private record ResultadoAnalise(List<TransacaoFaturaDTO> transacoes,
                                    BigDecimal valorTotalFatura,
                                    PadraoFatura padrao,
                                    boolean usouFallbackLocal) {
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

    private FaturaAnaliseResponseDTO respostaPrecisaHabilitarIa(boolean pdfEscaneado,
                                                                boolean ocrUtilizado,
                                                                String mensagem) {
        return new FaturaAnaliseResponseDTO(
                List.of(), "NENHUMA", null,
                BigDecimal.ZERO, null, null, false, null,
                pdfEscaneado, ocrUtilizado, false, false, true,
                mensagem
        );
    }

    private FaturaAnaliseResponseDTO montarResposta(List<TransacaoFaturaDTO> transacoes,
                                                    BigDecimal valorTotalFatura,
                                                    String fonte,
                                                    PadraoFatura padrao,
                                                    boolean pdfEscaneado,
                                                    boolean ocrUtilizado,
                                                    boolean consentimentoPdfIa,
                                                    String mensagem) {
        // Despesas somam e receitas (estornos/créditos) descontam do total da fatura.
        BigDecimal soma = somar(transacoes);

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
                false,
                mensagem
        );
    }
}
