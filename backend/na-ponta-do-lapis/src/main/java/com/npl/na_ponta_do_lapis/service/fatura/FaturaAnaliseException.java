package com.npl.na_ponta_do_lapis.service.fatura;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;

/**
 * Exceção de domínio da análise de faturas.
 *
 * Carrega o status HTTP e o código de erro que o frontend usa para decidir
 * o próximo passo (ex.: 428 -> pedir chave de IA).
 */
public class FaturaAnaliseException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;
    private final boolean chaveInvalida;
    private final List<String> provedores;

    public FaturaAnaliseException(HttpStatus status, String codigo, String mensagem) {
        this(status, codigo, mensagem, false, List.of());
    }

    public FaturaAnaliseException(HttpStatus status, String codigo, String mensagem,
                                  boolean chaveInvalida, List<String> provedores) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
        this.chaveInvalida = chaveInvalida;
        this.provedores = provedores == null ? List.of() : List.copyOf(provedores);
    }

    public static FaturaAnaliseException arquivoInvalido(String mensagem) {
        return new FaturaAnaliseException(HttpStatus.BAD_REQUEST, "ARQUIVO_INVALIDO", mensagem);
    }

    public static FaturaAnaliseException arquivoMuitoGrande() {
        return new FaturaAnaliseException(HttpStatus.PAYLOAD_TOO_LARGE, "ARQUIVO_MUITO_GRANDE",
                "O arquivo enviado excede o limite permitido de 10 MB.");
    }

    public static FaturaAnaliseException precisaIa() {
        List<String> provedores = Arrays.stream(ProvedorIa.values())
                .map(Enum::name)
                .toList();
        return new FaturaAnaliseException(HttpStatus.PRECONDITION_REQUIRED, "IA_NECESSARIA",
                "Não foi possível extrair as transações por regex. Informe uma chave de IA para continuar.",
                false, provedores);
    }

    public static FaturaAnaliseException chaveInvalida() {
        return new FaturaAnaliseException(HttpStatus.UNPROCESSABLE_ENTITY, "CHAVE_INVALIDA",
                "A chave de IA informada foi recusada pelo provedor. Verifique a chave e tente novamente.",
                true, List.of());
    }

    public static FaturaAnaliseException limiteIa() {
        return new FaturaAnaliseException(HttpStatus.TOO_MANY_REQUESTS, "LIMITE_IA",
                "O provedor de IA informou limite de uso. Aguarde alguns instantes e tente novamente.");
    }

    public static FaturaAnaliseException respostaIaInvalida(String detalhe) {
        return new FaturaAnaliseException(HttpStatus.BAD_GATEWAY, "RESPOSTA_IA_INVALIDA",
                "O provedor de IA retornou uma resposta que não pôde ser interpretada. " + detalhe);
    }

    public static FaturaAnaliseException provedorIndisponivel() {
        return new FaturaAnaliseException(HttpStatus.SERVICE_UNAVAILABLE, "PROVEDOR_INDISPONIVEL",
                "O provedor de IA está temporariamente indisponível. Tente novamente em alguns instantes.");
    }

    public static FaturaAnaliseException timeout() {
        return new FaturaAnaliseException(HttpStatus.GATEWAY_TIMEOUT, "TIMEOUT_IA",
                "O provedor de IA demorou demais para responder. Tente novamente.");
    }

    public static FaturaAnaliseException pdfSemTexto() {
        return new FaturaAnaliseException(HttpStatus.UNPROCESSABLE_ENTITY, "PDF_SEM_TEXTO",
                "Não foi possível extrair texto do PDF, nem por OCR local.");
    }

    public static FaturaAnaliseException consentimentoObrigatorio() {
        return new FaturaAnaliseException(HttpStatus.UNPROCESSABLE_ENTITY, "CONSENTIMENTO_OBRIGATORIO",
                "Para enviar o PDF à IA é necessário autorizar explicitamente o envio dos dados pessoais.");
    }

    public static FaturaAnaliseException provedorSemSuportePdf() {
        return new FaturaAnaliseException(HttpStatus.UNPROCESSABLE_ENTITY, "PROVEDOR_SEM_SUPORTE_PDF",
                "Este provedor não aceita PDF diretamente. Use Gemini ou Claude para o envio multimodal.");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }

    public boolean isChaveInvalida() {
        return chaveInvalida;
    }

    public List<String> getProvedores() {
        return provedores;
    }
}
