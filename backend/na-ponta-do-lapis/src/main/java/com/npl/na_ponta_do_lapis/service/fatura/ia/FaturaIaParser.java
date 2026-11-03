package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaRegexService;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

/**
 * Interpreta a resposta JSON dos provedores de IA e valida o padrão sugerido.
 */
@Service
public class FaturaIaParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FaturaIaParser.class);

    /** Descrições que a IA às vezes devolve, mas que não são transações. */
    private static final List<String> DESCARTAR_DESCRICAO = List.of(
            "parcelar em",
            "valor da parcela",
            "valor de entrada",
            "juros totais",
            "custo efetivo total",
            "cet ",
            "simulacao",
            "saldo em aberto",
            "saldo devedor",
            "proxima fatura",
            "pagamento minimo",
            "vencimento",
            "limite total",
            "limite disponivel",
            "limite de credito",
            "valor maximo",
            "saque no credito",
            "pix no credito",
            "boleto no credito",
            "juros de atraso",
            "multa de atraso",
            "iof de atraso",
            "credito de atraso",
            "encerramento de divida",
            "saldo em atraso"
    );

    private final ObjectMapper objectMapper;
    private final FaturaRegexService faturaRegexService;

    public FaturaIaParser(ObjectMapper objectMapper, FaturaRegexService faturaRegexService) {
        this.objectMapper = objectMapper;
        this.faturaRegexService = faturaRegexService;
    }

    public ResultadoIa parse(String resposta, String textoSanitizado, List<String> categoriasPermitidas) {
        if (resposta == null || resposta.isBlank()) {
            throw FaturaAnaliseException.respostaIaInvalida("Resposta vazia.");
        }

        JsonNode raiz;
        try {
            raiz = objectMapper.readTree(limparCercas(resposta));
        } catch (Exception e) {
            throw FaturaAnaliseException.respostaIaInvalida("JSON inválido.");
        }

        JsonNode erroNode = raiz.path("erro");
        if (!erroNode.isMissingNode() && "DOCUMENTO_NAO_EH_FATURA".equalsIgnoreCase(erroNode.asText())) {
            throw FaturaAnaliseException.documentoNaoEhFatura();
        }

        JsonNode arrayTransacoes = raiz.isArray() ? raiz : raiz.path("transacoes");
        if (!arrayTransacoes.isArray() || arrayTransacoes.isEmpty()) {
            throw FaturaAnaliseException.respostaIaInvalida("Nenhuma transação encontrada na resposta.");
        }

        List<TransacaoFaturaDTO> transacoes = new ArrayList<>();
        for (JsonNode node : arrayTransacoes) {
            TransacaoFaturaDTO transacao = parseTransacao(node, categoriasPermitidas);
            if (transacao != null) {
                transacoes.add(transacao);
            }
        }
        if (transacoes.isEmpty()) {
            throw FaturaAnaliseException.respostaIaInvalida("Nenhuma transação válida na resposta.");
        }

        String emissor = raiz.path("emissor").asText(null);
        if (emissor != null && emissor.isBlank()) {
            emissor = null;
        }
        BigDecimal total = parseValor(raiz.path("valorTotalFatura"));
        if (total == null) {
            total = parseValor(raiz.path("totalFatura"));
        }

        return new ResultadoIa(transacoes, total, emissor);
    }

    private TransacaoFaturaDTO parseTransacao(JsonNode node, List<String> categoriasPermitidas) {
        String descricaoBruta = node.path("descricao").asText(null);
        if (descricaoBruta == null || descricaoBruta.isBlank()) {
            return null;
        }
        String descricao = FaturaRegexService.limparDescricao(descricaoBruta);
        if (descricao.isBlank() || descricaoDeRuido(descricao)) {
            return null;
        }
        BigDecimal valor = parseValor(node.path("valor"));
        if (valor == null || valor.signum() == 0) {
            return null;
        }
        String dataTexto = node.path("data").asText(null);
        if (dataTexto == null || dataTexto.isBlank()) {
            return null;
        }
        if (dataTexto.length() > 10 && dataTexto.charAt(4) == '-') {
            dataTexto = dataTexto.substring(0, 10);
        }
        var data = faturaRegexService.parseData(dataTexto, null);
        if (data == null) {
            return null;
        }

        Integer numeroParcela = inteiroOuNulo(node.path("numeroParcela"));
        Integer totalParcelas = inteiroOuNulo(node.path("totalParcelas"));
        if (numeroParcela != null && totalParcelas != null
                && (numeroParcela < 1 || totalParcelas < 2
                    || numeroParcela > totalParcelas || totalParcelas > 60)) {
            numeroParcela = null;
            totalParcelas = null;
        }

        String categoria = escolherCategoria(node.path("categoria").asText(null), categoriasPermitidas);
        BigDecimal valorTotalCompra = parseValor(node.path("valorTotalCompra"));

        String tipoInformado = node.path("tipo").asText(null);
        boolean receita = "RECEITA".equalsIgnoreCase(tipoInformado)
                || valor.signum() < 0
                || FaturaRegexService.indicaReceita(descricao);

        return new TransacaoFaturaDTO(
                descricao,
                valor.abs(),
                data,
                categoria,
                receita ? TipoTransacao.RECEITA : TipoTransacao.DESPESA,
                numeroParcela,
                totalParcelas,
                valorTotalCompra
        );
    }

    private boolean descricaoDeRuido(String descricao) {
        String normalizada = FaturaRegexService.normalizar(descricao);
        return DESCARTAR_DESCRICAO.stream().anyMatch(normalizada::contains);
    }

    private String escolherCategoria(String categoria, List<String> categoriasPermitidas) {
        if (categoriasPermitidas == null || categoriasPermitidas.isEmpty()) {
            return categoria == null || categoria.isBlank() ? "Outros" : categoria.trim();
        }
        String normalizada = FaturaRegexService.normalizar(categoria);
        for (String permitida : categoriasPermitidas) {
            if (FaturaRegexService.normalizar(permitida).equals(normalizada)) {
                return permitida;
            }
        }
        for (String permitida : categoriasPermitidas) {
            if ("outros".equals(FaturaRegexService.normalizar(permitida))) {
                return permitida;
            }
        }
        return categoriasPermitidas.get(0);
    }

    private BigDecimal parseValor(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        return FaturaRegexService.parseValor(node.asText(null));
    }

    private Integer inteiroOuNulo(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isInt() || node.isLong()) {
            return node.asInt();
        }
        try {
            return Integer.parseInt(node.asText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String limparCercas(String resposta) {
        String limpa = resposta.trim();
        limpa = limpa.replaceAll("(?s)^```(?:json)?\\s*", "");
        limpa = limpa.replaceAll("(?s)\\s*```$", "");
        limpa = limpa.trim();

        // Alguns provedores adicionam texto antes/depois do JSON. Isola o
        // primeiro objeto (ou array, quando for o caso) para não perder a
        // resposta inteira por causa de uma frase de introdução.
        int inicio = limpa.indexOf('{');
        int fim = limpa.lastIndexOf('}');
        if (inicio < 0 || fim <= inicio) {
            inicio = limpa.indexOf('[');
            fim = limpa.lastIndexOf(']');
        }
        if (inicio >= 0 && fim > inicio) {
            limpa = limpa.substring(inicio, fim + 1);
        }
        return limpa.trim();
    }

    public record ResultadoIa(List<TransacaoFaturaDTO> transacoes,
                              BigDecimal valorTotalFatura,
                              String emissor) {
        public ResultadoIa(List<TransacaoFaturaDTO> transacoes, BigDecimal valorTotalFatura) {
            this(transacoes, valorTotalFatura, null);
        }
    }
}
