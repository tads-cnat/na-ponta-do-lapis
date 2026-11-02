package com.npl.na_ponta_do_lapis.service.fatura;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.PadraoFaturaRepository;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

import jakarta.annotation.PreDestroy;

/**
 * Extração local de faturas: texto do PDF + regex globais por emissor.
 *
 * O regex é gerado/calibrado também pela IA, por isso toda execução passa por
 * um limite de tempo (proteção simples contra ReDoS).
 */
@Service
public class FaturaRegexService {

    private static final Logger log = LoggerFactory.getLogger(FaturaRegexService.class);
    private static final Locale LOCALE_PT = Locale.forLanguageTag("pt-BR");
    private static final int REGEX_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE;

    private static final Pattern PARCELA = Pattern.compile("(?<!\\d)(\\d{1,2})\\s*/\\s*(\\d{1,2})(?!\\d)");
    private static final Pattern DATA_BR = Pattern.compile("^(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?$");
    private static final Pattern DATA_ISO = Pattern.compile("^(\\d{4})-(\\d{1,2})-(\\d{1,2})$");
    private static final Pattern DATA_MES = Pattern.compile("^(\\d{1,2})\\s+([A-Za-zÀ-ÿ]{3,})[.]?,?(?:\\s+(\\d{2,4}))?$");

    private static final List<String> LINHAS_IGNORADAS = List.of(
            "saldo anterior",
            "total da fatura",
            "total a pagar",
            "vencimento",
            "limite disponivel",
            "limite de credito",
            "credito disponivel",
            "proxima fatura"
    );

    private static final List<String> INDICADORES_DESPESA_FORCADA = List.of(
            "juros",
            "multa",
            "encargo",
            "tarifa",
            "pix credito",
            "pix no credito",
            "pix cred",
            "pix a vista",
            "saque credito",
            "saque no credito",
            "iof",
            "anuidade",
            "mercadolivre*reembolso",
            "mercadolivre reembolso",
            "mercadopago*reembolso",
            "mercadopago reembolso"
    );

    /**
     * Termos que indicam crédito na fatura (estornos, devoluções, reembolsos,
     * cancelamentos, créditos e pagamentos recebidos). Essas linhas entram como
     * RECEITA e são subtraídas da soma das compras.
     */
    private static final List<String> INDICADORES_RECEITA = List.of(
            "estorno",
            "estornado",
            "estornada",
            "devolucao",
            "reembolso",
            "chargeback",
            "cancelamento",
            "cancelado",
            "cancelada",
            "credito",
            "ajuste a credito",
            "saldo credor",
            "saldo a favor",
            "pagamento",
            "pagto",
            "payment"
    );

    /**
     * Linhas que contêm termos de pagamento mas não representam um pagamento
     * recebido (avisos, instruções e valores mínimos).
     */
    private static final List<String> LINHAS_NAO_PAGAMENTO = List.of(
            "pagamento minimo",
            "proximo pagamento",
            "data de pagamento",
            "data do pagamento",
            "local de pagamento",
            "forma de pagamento",
            "pagamento ate",
            "pagamento em atraso",
            "facilidade de pagamento",
            "valor minimo",
            "saldo devedor"
    );

    private static final Pattern VALOR_NA_LINHA = Pattern.compile(
            "(?:\\(\\s*)?(?:R\\$\\s*)?-?\\d{1,3}(?:\\.\\d{3})*,\\d{2}(?:\\s*\\))?(?:\\s*[CD])?");

    private static final Pattern LINHA_SO_VALOR = Pattern.compile(
            "^\\s*(?:\\(\\s*)?(?:R\\$\\s*)?-?\\d{1,3}(?:\\.\\d{3})*,\\d{2}(?:\\s*\\))?(?:\\s*[CD])?\\s*$");

    private static final Pattern DATA_NA_LINHA = Pattern.compile(
            "(?<![\\d/])(?:\\d{4}-\\d{1,2}-\\d{1,2}"
                    + "|\\d{1,2}/\\d{1,2}(?:/\\d{2,4})?"
                    + "|\\d{1,2}\\s+de\\s+[A-Za-zÀ-ÿ]{3,}\\.?(?:\\s+de\\s+\\d{2,4})?"
                    + "|\\d{1,2}\\s+[A-Za-zÀ-ÿ]{3,}\\.?(?:\\s+\\d{2,4})?)(?!\\d)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Data curta usada por faturas Nubank: "26 SET" (pode vir colada no texto). */
    private static final Pattern DATA_CURTA = Pattern.compile(
            "(\\d{1,2})\\s*(JAN|FEV|MAR|ABR|MAI|JUN|JUL|AGO|SET|OUT|NOV|DEZ)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Valor monetário com sinal opcional: "R$ 1.234,56" ou "−R$ 6,07". */
    private static final Pattern VALOR_TOKEN = Pattern.compile(
            "([-−]?)\\s*R\\$\\s*(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");

    private static final Pattern ANO_FATURA = Pattern.compile(
            "(?i)FATURA\\s+\\d{1,2}\\s+[A-Z]{3}\\s+(\\d{4})");

    private static final Pattern LINHA_TRANSACOES = Pattern.compile("(?im)^\\s*TRANSAÇÕES\\s*$");

    private static final String TOTAL_SUMARIO_TRANSACOES =
            "(?i)(?:[\\s\\S]{0,80}?)([-−]?\\s*R\\$\\s*\\d{1,3}(?:\\.\\d{3})*,\\d{2})";

    private static final BigDecimal UM_CENTAVO = new BigDecimal("0.01");

    private static final Map<String, Integer> MESES = Map.ofEntries(
            Map.entry("JAN", 1), Map.entry("JANEIRO", 1), Map.entry("JANUARY", 1),
            Map.entry("FEV", 2), Map.entry("FEVEREIRO", 2), Map.entry("FEB", 2), Map.entry("FEBRUARY", 2),
            Map.entry("MAR", 3), Map.entry("MARCO", 3), Map.entry("MARCH", 3),
            Map.entry("ABR", 4), Map.entry("ABRIL", 4), Map.entry("APR", 4), Map.entry("APRIL", 4),
            Map.entry("MAI", 5), Map.entry("MAIO", 5), Map.entry("MAY", 5),
            Map.entry("JUN", 6), Map.entry("JUNHO", 6), Map.entry("JUNE", 6),
            Map.entry("JUL", 7), Map.entry("JULHO", 7), Map.entry("JULY", 7),
            Map.entry("AGO", 8), Map.entry("AGOSTO", 8), Map.entry("AUG", 8), Map.entry("AUGUST", 8),
            Map.entry("SET", 9), Map.entry("SETEMBRO", 9), Map.entry("SEP", 9), Map.entry("SEPTEMBER", 9),
            Map.entry("OUT", 10), Map.entry("OUTUBRO", 10), Map.entry("OCT", 10), Map.entry("OCTOBER", 10),
            Map.entry("NOV", 11), Map.entry("NOVEMBRO", 11), Map.entry("NOVEMBER", 11),
            Map.entry("DEZ", 12), Map.entry("DEZEMBRO", 12), Map.entry("DEC", 12), Map.entry("DECEMBER", 12)
    );

    private static final Map<String, List<String>> CATEGORIAS = criarMapaCategorias();

    private static Map<String, List<String>> criarMapaCategorias() {
        Map<String, List<String>> categorias = new LinkedHashMap<>();
        categorias.put("Alimentação", List.of("mercado", "supermercado", "restaurante", "lanchonete", "padaria",
                "ifood", "rappi", "pizzaria", "burger", "cafe", "cafeteria", "acougue", "hortifruti",
                "mcdonald", "habib", "sushi", "food", "bar", "bebida", "doceria", "sorveteria"));
        categorias.put("Transporte", List.of("uber", "99", "cabify", "posto", "gasolina", "combustivel", "etanol",
                "diesel", "estacionamento", "pedagio", "sem parar", "metro", "onibus", "passagem",
                "latam", "gol", "azul", "taxi", "moto", "carro"));
        categorias.put("Saúde", List.of("farmacia", "drogaria", "hospital", "clinica", "laboratorio", "consulta",
                "dentista", "unimed", "amil", "bradesco saude", "academia", "fisioterapia", "psicologo",
                "exame", "vacina"));
        categorias.put("Educação", List.of("escola", "faculdade", "universidade", "curso", "livro", "livraria",
                "udemy", "alura", "mensalidade", "creche", "apostila", "material escolar"));
        categorias.put("Lazer", List.of("cinema", "netflix", "spotify", "disney", "prime video", "viagem", "hotel",
                "pousada", "parque", "show", "teatro", "jogo", "steam", "playstation", "xbox",
                "ingresso", "streaming"));
        return Collections.unmodifiableMap(categorias);
    }

    private static final String[] FORMATOS_DATA = {
            "dd/MM/yyyy", "dd/MM/yy", "dd/MM", "yyyy-MM-dd", "dd MMM yyyy", "dd MMM", "dd 'de' MMMM 'de' yyyy"
    };

    private static final ExecutorService REGEX_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "fatura-regex");
        thread.setDaemon(true);
        return thread;
    });

    private final PadraoFaturaRepository padraoFaturaRepository;

    @Value("${fatura.regex.timeout-ms:3000}")
    private long timeoutMs;

    @Value("${fatura.regex.max-text-chars:300000}")
    private int maxTextChars;

    public FaturaRegexService(PadraoFaturaRepository padraoFaturaRepository) {
        this.padraoFaturaRepository = padraoFaturaRepository;
    }

    @PreDestroy
    void encerrarExecutor() {
        REGEX_EXECUTOR.shutdownNow();
    }

    // -------------------------------------------------------------------------
    // Extração de texto
    // -------------------------------------------------------------------------

    public String extrairTexto(byte[] pdf) throws IOException {
        return extrairTexto(pdf, null);
    }

    public String extrairTexto(byte[] pdf, String senha) throws IOException {
        try (PDDocument documento = (senha != null && !senha.isBlank())
                ? Loader.loadPDF(pdf, senha)
                : Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setLineSeparator("\n");
            return limitarTexto(stripper.getText(documento));
        }
    }

    private String limitarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String limpo = texto.replace("\u0000", "").trim();
        return limpo.length() > maxTextChars ? limpo.substring(0, maxTextChars) : limpo;
    }

    // -------------------------------------------------------------------------
    // Análise por regex
    // -------------------------------------------------------------------------

    public ResultadoRegex analisar(String texto) {
        if (texto == null || texto.isBlank()) {
            return new ResultadoRegex(List.of(), null, null);
        }

        for (PadraoFatura padrao : padraoFaturaRepository.findByAtivoTrueOrderByVersaoDesc()) {
            if (!identifica(padrao, texto)) {
                continue;
            }

            BigDecimal total = extrairTotal(padrao, texto);
            ResumoFatura resumo = extrairResumo(texto);
            boolean leitorDeResumo = resumo != null
                    && resumo.totalPagar() != null
                    && ehFaturaNubank(texto);
            List<TransacaoFaturaDTO> transacoes = List.of();

            if (leitorDeResumo) {
                // Faturas Nubank têm o texto embaralhado pelo extrator de PDF.
                // Lemos as compras linha a linha e usamos o resumo para
                // conciliar o rodapé da fatura.
                transacoes = extrairTransacoesPorLinha(texto);
            }
            if (transacoes.isEmpty()) {
                // Layout diferente (ou leitor de resumo sem seção de
                // transações): usa o regex do padrão salvo.
                leitorDeResumo = false;
                transacoes = aplicarPadrao(padrao, texto);
            }

            if (!transacoes.isEmpty()) {
                List<TransacaoFaturaDTO> completas = new ArrayList<>(transacoes);
                if (!leitorDeResumo) {
                    completas.addAll(extrairPagamentosECreditos(texto, completas));
                }
                completas = reconciliar(texto, completas, total);
                return new ResultadoRegex(completas, padrao, total);
            }
        }

        // Nenhum padrão reconheceu a fatura, mas o resumo ainda permite ler as
        // transações e conciliar o total (ex.: banco sem padrão cadastrado).
        ResumoFatura resumo = extrairResumo(texto);
        if (resumo != null && resumo.totalPagar() != null) {
            List<TransacaoFaturaDTO> transacoes = extrairTransacoesPorLinha(texto);
            if (!transacoes.isEmpty()) {
                BigDecimal total = resumo.totalPagar().abs();
                PadraoFatura padraoEmissor = null;
                if (ehFaturaNubank(texto)) {
                    padraoEmissor = padraoFaturaRepository
                            .findByEmissorAndAtivoTrueOrderByVersaoDesc("Nubank")
                            .stream().findFirst().orElse(null);
                }
                return new ResultadoRegex(reconciliar(texto, transacoes, total), padraoEmissor, total);
            }
        }
        return new ResultadoRegex(List.of(), null, null);
    }

    /**
     * Indica se a fatura tem um resumo que o leitor local consegue reconciliar.
     * Nesses casos não vale a pena salvar um regex gerado pela IA, pois o
     * leitor dedicado já garante que a soma fecha com o total.
     */
    public boolean possuiLeituraDeResumoConfiavel(String texto) {
        if (texto == null || texto.isBlank() || !ehFaturaNubank(texto)) {
            return false;
        }
        ResumoFatura resumo = extrairResumo(texto);
        return resumo != null
                && resumo.totalPagar() != null
                && indiceSecaoTransacoes(texto) >= 0;
    }

    public ResumoFatura extrairResumoFatura(String texto) {
        return extrairResumo(texto);
    }

    /**
     * Aplica a conciliação do resumo a transações vindas de outra fonte (ex.: IA).
     */
    public List<TransacaoFaturaDTO> conciliarComResumo(String texto,
                                                       List<TransacaoFaturaDTO> transacoes,
                                                       BigDecimal total) {
        return reconciliar(texto, transacoes, total);
    }

    private boolean ehFaturaNubank(String texto) {
        String normalizado = normalizar(texto);
        return normalizado.contains("nubank") || normalizado.contains("nu pagamentos");
    }

    public Optional<PadraoFatura> identificarPadrao(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        return padraoFaturaRepository.findByAtivoTrueOrderByVersaoDesc().stream()
                .filter(p -> identifica(p, texto))
                .findFirst();
    }

    private boolean identifica(PadraoFatura padrao, String texto) {
        if (padrao.getRegexIdentificacao() == null || padrao.getRegexIdentificacao().isBlank()) {
            return false;
        }
        Optional<Boolean> encontrado = executarComTimeout(() ->
                Pattern.compile(padrao.getRegexIdentificacao(), REGEX_FLAGS).matcher(texto).find());
        return encontrado.orElse(false);
    }

    public List<TransacaoFaturaDTO> aplicarPadrao(PadraoFatura padrao, String texto) {
        if (padrao == null || padrao.getRegexTransacao() == null || texto == null || texto.isBlank()) {
            return List.of();
        }
        if (padrao.getRegexTransacao().length() > 1000) {
            log.warn("Padrão de fatura ignorado: regex de transação maior que 1000 caracteres.");
            return List.of();
        }

        Optional<List<TransacaoFaturaDTO>> resultado = executarComTimeout(() -> {
            List<TransacaoFaturaDTO> transacoes = new ArrayList<>();
            Pattern pattern;
            try {
                pattern = Pattern.compile(padrao.getRegexTransacao(), REGEX_FLAGS);
            } catch (PatternSyntaxException e) {
                return transacoes;
            }
            if (!pattern.namedGroups().containsKey("descricao")
                    || !pattern.namedGroups().containsKey("data")
                    || !pattern.namedGroups().containsKey("valor")) {
                return transacoes;
            }

            // Isola o texto da fatura atual se houver uma seção explícita de 'Próxima fatura'
            String textoAlvo = texto;
            Matcher mProxima = Pattern.compile("(?im)^\\s*próximas?\\s+faturas?\\s*$").matcher(texto);
            if (mProxima.find()) {
                textoAlvo = texto.substring(0, mProxima.start());
            }

            Matcher matcher = pattern.matcher(textoAlvo);
            while (matcher.find()) {
                String descricao = matcher.group("descricao");
                String data = matcher.group("data");
                String valor = matcher.group("valor");
                String linha = matcher.group();

                if (linha.contains("+ R$") || linha.contains("+R$") || linha.matches("(?i).*\\+\\s*R\\$.*")) {
                    continue;
                }

                if (descricao == null || valor == null || ignorarLinha(descricao) || ignorarLinha(linha)) {
                    continue;
                }
                BigDecimal valorNumerico = parseValor(valor);
                if (valorNumerico == null || valorNumerico.signum() == 0) {
                    continue;
                }
                LocalDate dataNumerica = parseData(data, padrao.getFormatoData());
                if (dataNumerica == null) {
                    continue;
                }

                Parcela parcela = extrairParcela(descricao);
                String descricaoLimpa = limparDescricao(descricao);
                if (descricaoLimpa.isBlank()) {
                    continue;
                }

                boolean receita = valorNumerico.signum() < 0
                        || indicaReceita(descricaoLimpa)
                        || indicaReceita(linha);

                transacoes.add(new TransacaoFaturaDTO(
                        descricaoLimpa,
                        valorNumerico.abs(),
                        dataNumerica,
                        classificarCategoria(descricaoLimpa),
                        receita ? TipoTransacao.RECEITA : TipoTransacao.DESPESA,
                        parcela.numero(),
                        parcela.total(),
                        null
                ));
            }
            return transacoes;
        });

        return resultado.orElse(List.of());
    }

    public BigDecimal extrairTotal(PadraoFatura padrao, String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        // 1) O resumo da fatura é a fonte primária se for fatura Nubank (única com a tabela de resumo padronizada).
        if (ehFaturaNubank(texto)) {
            ResumoFatura resumo = extrairResumo(texto);
            if (resumo != null && resumo.totalPagar() != null) {
                return resumo.totalPagar().abs();
            }
        }

        // 2) Regex do padrão salvo específico do emissor (ex: Inter, Itaú, etc.).
        if (padrao != null && padrao.getRegexTotal() != null && !padrao.getRegexTotal().isBlank()) {
            Optional<BigDecimal> totalDoPadrao = executarComTimeout(() -> {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(padrao.getRegexTotal(), REGEX_FLAGS);
                } catch (PatternSyntaxException e) {
                    return null;
                }
                Matcher matcher = pattern.matcher(texto);
                while (matcher.find()) {
                    if (matcher.groupCount() < 1) {
                        continue;
                    }
                    BigDecimal valor = parseValor(matcher.group(1));
                    if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    String contexto = normalizar(texto.substring(Math.max(0, matcher.start() - 60), matcher.start()));
                    if (contexto.contains("saldo em aberto")
                            || contexto.contains("saldo devedor")
                            || contexto.contains("proxima fatura")
                            || contexto.contains("limite")
                            || contexto.contains("juros")
                            || contexto.contains("encargos")
                            || contexto.contains("multa")) {
                        continue;
                    }
                    return valor.abs();
                }
                return null;
            });
            if (totalDoPadrao.isPresent() && totalDoPadrao.get() != null) {
                return totalDoPadrao.get();
            }
        }

        // 3) Rótulos explícitos de total (evita "saldo em aberto total" e encargos).
        BigDecimal explicito = extrairTotalExplicito(texto);
        if (explicito != null) {
            return explicito.abs();
        }

        return null;
    }

    /**
     * Procura o total por rótulos conhecidos, na ordem de maior confiança.
     * O primeiro valor encontrado é usado (e não o último, que costuma ser um
     * saldo devedor ou o saldo da próxima fatura).
     */
    private BigDecimal extrairTotalExplicito(String texto) {
        List<String> rotulos = List.of(
                "(?i)total\\s+a\\s+pagar\\s*:?",
                "(?i)total\\s+da\\s+fatura\\s*:?",
                "(?i)valor\\s+total\\s+(?:da\\s+fatura\\s*)?:?",
                "(?i)total\\s+desta\\s+fatura\\s*:?",
                "(?i)total\\s+do\\s+cartao\\s*:?",
                "(?i)fatura\\s+atual\\s*:?",
                "(?i)no\\s+valor\\s+de\\s*:?"
        );
        for (String rotulo : rotulos) {
            Matcher matcher = Pattern.compile(rotulo + TOTAL_SUMARIO_TRANSACOES, REGEX_FLAGS).matcher(texto);
            while (matcher.find()) {
                String trechoCasado = normalizar(matcher.group());
                if (trechoCasado.contains("encargos")
                        || trechoCasado.contains("rotativo")
                        || trechoCasado.contains("juros")
                        || trechoCasado.contains("multa")
                        || trechoCasado.contains("iof")) {
                    continue;
                }
                String contexto = normalizar(texto.substring(Math.max(0, matcher.start() - 60), matcher.start()));
                if (contexto.contains("saldo em aberto")
                        || contexto.contains("saldo devedor")
                        || contexto.contains("proxima fatura")
                        || contexto.contains("limite")
                        || contexto.contains("juros")
                        || contexto.contains("encargos")
                        || contexto.contains("multa")) {
                    continue;
                }
                BigDecimal valor = parseValor(matcher.group(1));
                if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
                    return valor;
                }
            }
        }
        return null;
    }

    public boolean validarPadrao(PadraoFatura padrao, String texto) {
        if (padrao == null
                || padrao.getRegexIdentificacao() == null || padrao.getRegexIdentificacao().isBlank()
                || padrao.getRegexTransacao() == null || padrao.getRegexTransacao().isBlank()
                || padrao.getRegexIdentificacao().length() > 500
                || padrao.getRegexTransacao().length() > 1000) {
            return false;
        }

        try {
            Pattern identificacao = Pattern.compile(padrao.getRegexIdentificacao(), REGEX_FLAGS);
            Pattern transacao = Pattern.compile(padrao.getRegexTransacao(), REGEX_FLAGS);
            if (!transacao.namedGroups().containsKey("descricao")
                    || !transacao.namedGroups().containsKey("data")
                    || !transacao.namedGroups().containsKey("valor")) {
                return false;
            }
            if (padrao.getRegexTotal() != null && !padrao.getRegexTotal().isBlank()) {
                Pattern.compile(padrao.getRegexTotal(), REGEX_FLAGS);
            }
            if (texto == null || texto.isBlank() || !identificacao.matcher(texto).find()) {
                return false;
            }
        } catch (PatternSyntaxException e) {
            return false;
        }

        List<TransacaoFaturaDTO> transacoes = aplicarPadrao(padrao, texto);
        if (transacoes.isEmpty()) {
            return false;
        }

        BigDecimal total = extrairTotal(padrao, texto);
        if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal soma = somar(transacoes);
            BigDecimal diferenca = total.subtract(soma).abs();
            BigDecimal limiteAceitavel = total.multiply(new BigDecimal("0.05")).max(new BigDecimal("5.00"));
            if (diferenca.compareTo(limiteAceitavel) > 0) {
                log.warn("Padrão de fatura rejeitado na validação contábil: total R$ {}, soma R$ {}, diferença R$ {}",
                        total, soma, diferenca);
                return false;
            }
        }

        return true;
    }

    /**
     * Evita que um padrão novo (normalmente gerado pela IA) substitua um padrão
     * ativo que lê mais transações na mesma fatura.
     */
    public boolean padraoPiorQueAtivo(PadraoFatura novo, String texto, int transacoesEsperadas) {
        List<TransacaoFaturaDTO> novas = aplicarPadrao(novo, texto);
        if (novas.isEmpty()) {
            return true;
        }
        if (transacoesEsperadas >= 10 && novas.size() < transacoesEsperadas / 2) {
            return true;
        }
        if (novo.getEmissor() == null || novo.getEmissor().isBlank()) {
            return false;
        }
        List<PadraoFatura> ativos = padraoFaturaRepository
                .findByEmissorAndAtivoTrueOrderByVersaoDesc(novo.getEmissor());
        if (ativos == null || ativos.isEmpty()) {
            return false;
        }
        for (PadraoFatura ativo : ativos) {
            List<TransacaoFaturaDTO> ativas = aplicarPadrao(ativo, texto);
            BigDecimal totalAtivo = extrairTotal(ativo, texto);
            boolean ativoBate = totalAtivo != null
                    && somar(ativas).subtract(totalAtivo).abs().compareTo(UM_CENTAVO) <= 0;

            BigDecimal totalNovo = extrairTotal(novo, texto);
            boolean novoBate = totalNovo != null
                    && somar(novas).subtract(totalNovo).abs().compareTo(UM_CENTAVO) <= 0;

            if (novoBate && !ativoBate) {
                // O padrão sugerido pela IA fecha 100% com o total da fatura e o ativo atual não!
                continue;
            }
            if (ativas.size() > novas.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean ignorarLinha(String texto) {
        if (texto == null) {
            return true;
        }
        String normalizado = normalizar(texto);
        return LINHAS_IGNORADAS.stream().anyMatch(normalizado::contains);
    }

    private boolean ignorarLinhaPagamento(String normalizado) {
        return LINHAS_NAO_PAGAMENTO.stream().anyMatch(normalizado::contains);
    }

    /**
     * Captura pagamentos de fatura e créditos que o regex do emissor não pegou
     * (por exemplo, linhas sem data ou com a data fora do início da linha).
     * Essas linhas entram como RECEITA e reduzem o total calculado.
     */
    private List<TransacaoFaturaDTO> extrairPagamentosECreditos(String texto,
                                                                List<TransacaoFaturaDTO> existentes) {
        List<TransacaoFaturaDTO> extras = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return extras;
        }

        LocalDate dataReferencia = existentes.stream()
                .map(TransacaoFaturaDTO::data)
                .filter(data -> data != null)
                .max(LocalDate::compareTo)
                .orElseGet(LocalDate::now);

        String[] linhas = texto.split("\\R");
        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i] == null ? "" : linhas[i].trim();
            if (linha.isEmpty()) {
                continue;
            }

            String normalizada = normalizar(linha);
            if (!indicaReceita(normalizada)
                    || ignorarLinha(linha)
                    || ignorarLinhaPagamento(normalizada)
                    || linha.contains("+ R$")
                    || linha.contains("+R$")
                    || linha.matches("(?i).*\\+\\s*R\\$.*")) {
                continue;
            }

            String textoBase = linha;
            BigDecimal valor = ultimoValor(linha);
            if (valor == null && i + 1 < linhas.length) {
                String proxima = linhas[i + 1] == null ? "" : linhas[i + 1].trim();
                if (LINHA_SO_VALOR.matcher(proxima).matches()) {
                    valor = ultimoValor(proxima);
                    textoBase = linha + " " + proxima;
                    i++;
                }
            }
            if (valor == null || valor.signum() == 0) {
                continue;
            }

            LocalDate data = dataNaLinha(textoBase);
            if (data == null) {
                data = dataReferencia;
            }

            String descricao = descricaoPagamento(linha);
            if (descricao.isBlank()) {
                descricao = "Pagamento";
            }

            if (jaExiste(existentes, descricao, valor, data) || jaExiste(extras, descricao, valor, data)) {
                continue;
            }

            extras.add(new TransacaoFaturaDTO(
                    descricao,
                    valor.abs(),
                    data,
                    "Outros",
                    TipoTransacao.RECEITA,
                    null,
                    null,
                    null
            ));
        }
        return extras;
    }

    private BigDecimal ultimoValor(String linha) {
        Matcher matcher = VALOR_NA_LINHA.matcher(linha);
        BigDecimal ultimo = null;
        while (matcher.find()) {
            BigDecimal valor = parseValor(matcher.group());
            if (valor != null) {
                ultimo = valor;
            }
        }
        return ultimo;
    }

    private LocalDate dataNaLinha(String texto) {
        Matcher matcher = DATA_NA_LINHA.matcher(texto);
        while (matcher.find()) {
            LocalDate data = parseData(matcher.group(), null);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    private String descricaoPagamento(String linha) {
        String descricao = VALOR_NA_LINHA.matcher(linha).replaceAll(" ");
        descricao = DATA_NA_LINHA.matcher(descricao).replaceAll(" ");
        descricao = descricao.replaceAll("(?i)R\\$", " ");
        descricao = descricao.replaceAll("(?i)\\bem\\b", " ");
        return limparDescricao(descricao);
    }

    private boolean jaExiste(List<TransacaoFaturaDTO> transacoes, String descricao,
                             BigDecimal valor, LocalDate data) {
        String alvo = normalizar(descricao);
        if (alvo.isBlank()) {
            return false;
        }
        return transacoes.stream().anyMatch(t -> {
            if (t == null || t.valor() == null || t.data() == null || !t.data().equals(data)) {
                return false;
            }
            if (t.valor().abs().compareTo(valor.abs()) != 0) {
                return false;
            }
            String existente = normalizar(t.descricao());
            return existente.contains(alvo) || alvo.contains(existente);
        });
    }

    // -------------------------------------------------------------------------
    // Resumo da fatura (Nubank e similares)
    // -------------------------------------------------------------------------

    private ResumoFatura extrairResumo(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String normalizado = normalizar(texto);
        int inicio = normalizado.indexOf("resumo da fatura");
        if (inicio < 0) {
            return null;
        }
        int fim = normalizado.indexOf("proximas faturas", inicio);
        if (fim < 0) {
            fim = Math.min(normalizado.length(), inicio + 2500);
        }
        String bloco = normalizado.substring(inicio, fim);

        BigDecimal faturaAnterior = valorDoRotulo(bloco, "fatura anterior");
        BigDecimal pagamentoRecebido = valorDoRotulo(bloco, "pagamento recebido");
        BigDecimal totalCompras = valorDoRotulo(bloco, "total de compras");
        BigDecimal iofInternacional = valorDoRotulo(bloco, "iof de compras internacionais");
        BigDecimal outrosLancamentos = valorDoRotulo(bloco, "outros lancamentos");
        BigDecimal totalPagar = valorDoRotulo(bloco, "total a pagar");

        if (totalPagar == null && totalCompras == null) {
            return null;
        }
        return new ResumoFatura(faturaAnterior, pagamentoRecebido, totalCompras,
                iofInternacional, outrosLancamentos, totalPagar);
    }

    private BigDecimal valorDoRotulo(String bloco, String rotulo) {
        Pattern pattern = Pattern.compile(Pattern.quote(rotulo) + TOTAL_SUMARIO_TRANSACOES);
        Matcher matcher = pattern.matcher(bloco);
        if (matcher.find()) {
            return parseValor(matcher.group(1));
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Leitura linha a linha da seção de transações
    // -------------------------------------------------------------------------

    /**
     * Lê a seção de transações quando o PDF entrega datas, descrições e valores
     * em linhas separadas ou concatenadas (layout comum do Nubank). O objetivo é
     * capturar todos os valores; a descrição é associada ao texto mais próximo.
     */
    private List<TransacaoFaturaDTO> extrairTransacoesPorLinha(String texto) {
        List<TransacaoFaturaDTO> transacoes = new ArrayList<>();
        int inicio = indiceSecaoTransacoes(texto);
        if (inicio < 0) {
            return transacoes;
        }
        int fim = texto.indexOf("Pagamentos e Financiamentos", inicio);
        if (fim < 0) {
            fim = texto.length();
        }

        String secao = texto.substring(inicio, fim);
        String[] linhas = secao.split("\\R");
        int ano = inferirAno(texto);
        LocalDate dataAtual = null;
        String descricaoPendente = null;
        Deque<String> descricoesPendentes = new ArrayDeque<>();
        boolean aguardandoSubtotal = false;

        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i] == null ? "" : linhas[i].trim();
            if (linha.isEmpty() || ehRuidoDeSecao(linha)) {
                continue;
            }

            List<DataToken> datas = datasNaLinha(linha);
            List<ValorToken> valores = valoresNaLinha(linha);

            if (aguardandoSubtotal) {
                aguardandoSubtotal = false;
                if (datas.isEmpty() && valores.size() == 1) {
                    continue;
                }
            }

            if (datas.isEmpty() && valores.isEmpty() && pareceTituloSubsecao(linha)) {
                int proxima = proximaLinhaNaoVazia(linhas, i + 1);
                if (proxima >= 0) {
                    String textoProximo = linhas[proxima].trim();
                    if (datasNaLinha(textoProximo).isEmpty()
                            && valoresNaLinha(textoProximo).size() == 1) {
                        aguardandoSubtotal = true;
                        descricaoPendente = null;
                        descricoesPendentes.clear();
                        continue;
                    }
                }
            }

            if (!datas.isEmpty()) {
                DataToken ultimaData = datas.get(datas.size() - 1);
                dataAtual = criarData(ano, ultimaData.mes(), ultimaData.dia());

                if (!valores.isEmpty()) {
                    List<Segmento> segmentos = segmentosPorData(linha, datas);
                    List<Segmento> semValor = new ArrayList<>();
                    for (Segmento segmento : segmentos) {
                        if (segmento.valores().isEmpty()) {
                            semValor.add(segmento);
                            continue;
                        }
                        LocalDate dataSegmento = criarData(ano, segmento.mes(), segmento.dia());
                        for (ValorToken valor : segmento.valores()) {
                            adicionarTransacao(transacoes, segmento.descricao(), valor, dataSegmento);
                        }
                    }
                    if (!semValor.isEmpty()) {
                        descricaoPendente = semValor.get(semValor.size() - 1).descricao();
                    } else {
                        descricaoPendente = null;
                        descricoesPendentes.clear();
                    }
                } else {
                    String resto = linha.substring(ultimaData.fim());
                    String descricao = limparDescricaoLinha(resto);
                    descricaoPendente = descricao.isBlank() ? null : descricao;
                    descricoesPendentes.clear();
                    if (descricaoPendente != null) {
                        descricoesPendentes.addLast(descricaoPendente);
                    }
                }
                continue;
            }

            if (!valores.isEmpty()) {
                Deque<String> fila = new ArrayDeque<>(descricoesPendentes);
                if (descricaoPendente != null && !fila.contains(descricaoPendente)) {
                    fila.addFirst(descricaoPendente);
                }

                Deque<String> creditos = new ArrayDeque<>();
                Deque<String> outros = new ArrayDeque<>();
                for (String descricao : fila) {
                    if (indicaCreditoDescricao(descricao)) {
                        creditos.addLast(descricao);
                    } else {
                        outros.addLast(descricao);
                    }
                }

                List<ParValorDesc> pares = new ArrayList<>();
                for (ValorToken valor : valores) {
                    if (!valor.negativo()) {
                        continue;
                    }
                    String descricao = creditos.pollFirst();
                    if (descricao == null) {
                        descricao = outros.pollFirst();
                    }
                    pares.add(new ParValorDesc(valor, descricao));
                }
                for (ValorToken valor : valores) {
                    if (valor.negativo()) {
                        continue;
                    }
                    String descricao = outros.pollFirst();
                    if (descricao == null) {
                        descricao = creditos.pollFirst();
                    }
                    pares.add(new ParValorDesc(valor, descricao));
                }
                pares.sort(Comparator.comparingInt(par -> par.valor().inicio()));

                for (ParValorDesc par : pares) {
                    String descricao = par.descricao();
                    if (descricao == null || descricao.isBlank()) {
                        descricao = par.valor().negativo() ? "Crédito" : "Compra";
                    }
                    adicionarTransacao(transacoes, descricao, par.valor(), dataAtual);
                }

                descricoesPendentes.clear();
                descricoesPendentes.addAll(creditos);
                descricoesPendentes.addAll(outros);
                descricaoPendente = descricoesPendentes.peekFirst();
                continue;
            }

            String descricao = limparDescricaoLinha(linha);
            if (descricao.isBlank()) {
                continue;
            }
            if (descricaoPendente == null) {
                descricaoPendente = descricao;
                descricoesPendentes.addLast(descricao);
            } else if (!descricao.equals(descricaoPendente)) {
                descricoesPendentes.addLast(descricao);
            }
        }
        return transacoes;
    }

    private int indiceSecaoTransacoes(String texto) {
        int resumo = normalizar(texto).indexOf("resumo da fatura");
        if (resumo < 0) {
            return -1;
        }
        Matcher matcher = LINHA_TRANSACOES.matcher(texto);
        if (matcher.find(resumo)) {
            return matcher.start();
        }
        return -1;
    }

    private int inferirAno(String texto) {
        Matcher matcher = ANO_FATURA.matcher(texto);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                // usa o ano atual
            }
        }
        return LocalDate.now().getYear();
    }

    private List<DataToken> datasNaLinha(String linha) {
        List<DataToken> datas = new ArrayList<>();
        Matcher matcher = DATA_CURTA.matcher(linha);
        while (matcher.find()) {
            Integer mes = MESES.get(matcher.group(2).toUpperCase(Locale.ROOT));
            if (mes == null) {
                continue;
            }
            try {
                datas.add(new DataToken(matcher.start(), matcher.end(),
                        Integer.parseInt(matcher.group(1)), mes));
            } catch (NumberFormatException ignored) {
                // ignora token inválido
            }
        }
        return datas;
    }

    private List<ValorToken> valoresNaLinha(String linha) {
        List<ValorToken> valores = new ArrayList<>();
        Matcher matcher = VALOR_TOKEN.matcher(linha);
        while (matcher.find()) {
            BigDecimal valor = parseValor(matcher.group(2));
            if (valor != null && valor.signum() != 0) {
                boolean negativo = !matcher.group(1).isEmpty();
                valores.add(new ValorToken(matcher.start(), matcher.end(), valor.abs(), negativo));
            }
        }
        return valores;
    }

    private List<Segmento> segmentosPorData(String linha, List<DataToken> datas) {
        List<Segmento> segmentos = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            DataToken data = datas.get(i);
            int fimSegmento = i + 1 < datas.size() ? datas.get(i + 1).inicio() : linha.length();
            String trecho = linha.substring(data.fim(), fimSegmento);
            List<ValorToken> valores = valoresNaLinha(trecho);
            String descricao = trecho;
            if (!valores.isEmpty()) {
                descricao = trecho.substring(0, valores.get(0).inicio());
            }
            segmentos.add(new Segmento(data.dia(), data.mes(),
                    limparDescricaoLinha(descricao), valores));
        }
        return segmentos;
    }

    private String limparDescricaoLinha(String texto) {
        if (texto == null) {
            return "";
        }
        String limpa = DATA_CURTA.matcher(texto).replaceAll(" ");
        limpa = limpa.replaceAll("[•·]+\\s*\\d{4}", " ");
        limpa = limpa.replaceAll("^\\s*\\d{4}(?=[A-Za-zÀ-ÿ])", " ");
        limpa = limpa.replaceAll("\\d{2,3}\\.\\d{3}\\.\\d{3}", " ");
        limpa = limpa.replaceAll("\\d{2,3}\\s+\\d{3}\\s+\\d{3}", " ");
        limpa = limpa.replaceAll("(?i)R\\$", " ");
        limpa = limpa.replaceAll("\\s+", " ").trim();
        limpa = limpa.replaceAll("^[\\s:;,.\\-–—]+", "");
        limpa = limpa.replaceAll("[\\s:;,.\\-–—]+$", "");
        return limpa.trim();
    }

    private boolean pareceTituloSubsecao(String linha) {
        String texto = linha.trim();
        if (texto.isEmpty() || texto.length() > 90 || texto.matches(".*[0-9].*")) {
            return false;
        }
        String normalizado = normalizar(texto);
        if (normalizado.contains("iof")
                || normalizado.contains("credito")
                || normalizado.contains("estorno")
                || normalizado.contains("devolu")
                || normalizado.contains("reembolso")
                || normalizado.contains("pagamento")
                || normalizado.contains("juros")
                || normalizado.contains("multa")
                || normalizado.contains("saldo")
                || normalizado.contains("referente")
                || normalizado.contains("conversao")) {
            return false;
        }
        String[] palavras = texto.split("\\s+");
        if (palavras.length < 2) {
            return false;
        }
        int maiusculas = 0;
        for (String palavra : palavras) {
            if (!palavra.isEmpty() && Character.isUpperCase(palavra.charAt(0))) {
                maiusculas++;
            }
        }
        return maiusculas >= 2;
    }

    private int proximaLinhaNaoVazia(String[] linhas, int inicio) {
        for (int i = inicio; i < linhas.length; i++) {
            if (linhas[i] != null && !linhas[i].trim().isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private boolean ehRuidoDeSecao(String linha) {
        String texto = linha.trim();
        if (texto.matches("(?i)^\\d+\\s+de\\s+\\d+.*")) {
            return true;
        }
        if (texto.matches("(?i)^(MILENA|EMISSÃO E ENVIO.*|FATURA .*|DE .* A .*|TRANSAÇÕES|VALOR MÁXIMO.*)$")) {
            return true;
        }
        if (texto.contains("USD") || texto.matches("(?i).*Convers[aã]o:.*")) {
            return true;
        }
        return texto.matches("(?i)^(Referente ao valor.*|Valor original:.*|Saldo em aberto de.*)");
    }

    private void adicionarTransacao(List<TransacaoFaturaDTO> transacoes, String descricaoBruta,
                                    ValorToken valor, LocalDate data) {
        if (valor == null || valor.valor() == null || valor.valor().signum() == 0) {
            return;
        }
        String descricao = descricaoBruta == null ? "" : descricaoBruta;
        Parcela parcela = extrairParcela(descricao);
        String limpa = limparDescricao(descricao);
        if (limpa.isBlank()) {
            limpa = valor.negativo() ? "Crédito" : "Compra";
        }
        boolean receita = valor.negativo() || indicaReceita(limpa);
        transacoes.add(new TransacaoFaturaDTO(
                limpa,
                valor.valor().abs(),
                data,
                classificarCategoria(limpa),
                receita ? TipoTransacao.RECEITA : TipoTransacao.DESPESA,
                parcela.numero(),
                parcela.total(),
                null
        ));
    }

    private boolean indicaCreditoDescricao(String descricao) {
        String normalizada = normalizar(descricao);
        return normalizada.contains("credito")
                || normalizada.contains("estorno")
                || normalizada.contains("devolu")
                || normalizada.contains("reembolso")
                || normalizada.contains("cancelamento");
    }

    // -------------------------------------------------------------------------
    // Conciliação com o resumo
    // -------------------------------------------------------------------------

    /**
     * Garante que a soma das transações (despesas − receitas) seja exatamente o
     * total da fatura. Componentes do resumo que não aparecem itemizados entram
     * como transações próprias; qualquer centavo restante vira um ajuste claro.
     */
    private List<TransacaoFaturaDTO> reconciliar(String texto, List<TransacaoFaturaDTO> transacoes,
                                                  BigDecimal total) {
        if (!ehFaturaNubank(texto)) {
            return transacoes;
        }
        ResumoFatura resumo = extrairResumo(texto);
        if (resumo == null || total == null) {
            return transacoes;
        }

        List<TransacaoFaturaDTO> base = new ArrayList<>();
        for (TransacaoFaturaDTO transacao : transacoes) {
            if (transacao == null || transacao.valor() == null) {
                continue;
            }
            if (ehLinhaDePagamentoOuFinanciamento(transacao.descricao())) {
                continue;
            }
            if (ehResumoDuplicado(transacao.descricao())) {
                continue;
            }
            base.add(transacao);
        }

        LocalDate dataReferencia = base.stream()
                .map(TransacaoFaturaDTO::data)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElseGet(LocalDate::now);

        List<TransacaoFaturaDTO> sinteticas = new ArrayList<>();
        if (resumo.faturaAnterior() != null && !temDescricao(base, "fatura anterior")) {
            sinteticas.add(criarSintetica("Fatura anterior", resumo.faturaAnterior().abs(),
                    dataReferencia, TipoTransacao.DESPESA));
        }
        if (resumo.pagamentoRecebido() != null && !temDescricao(base, "pagamento recebido")) {
            sinteticas.add(criarSintetica("Pagamento recebido", resumo.pagamentoRecebido().abs(),
                    dataReferencia, TipoTransacao.RECEITA));
        }
        if (resumo.iofInternacional() != null && !temDescricao(base, "iof")) {
            sinteticas.add(criarSintetica("IOF de compras internacionais",
                    resumo.iofInternacional().abs(), dataReferencia, TipoTransacao.DESPESA));
        }
        if (resumo.outrosLancamentos() != null && resumo.outrosLancamentos().signum() != 0) {
            BigDecimal valor = resumo.outrosLancamentos();
            sinteticas.add(criarSintetica("Outros lançamentos", valor.abs(), dataReferencia,
                    valor.signum() < 0 ? TipoTransacao.RECEITA : TipoTransacao.DESPESA));
        }

        BigDecimal soma = somar(base).add(somar(sinteticas));
        BigDecimal residuo = total.subtract(soma);
        if (residuo.abs().compareTo(new BigDecimal("0.005")) > 0) {
            sinteticas.add(criarSintetica("Ajuste de conciliação da fatura", residuo.abs(),
                    dataReferencia, residuo.signum() > 0 ? TipoTransacao.DESPESA : TipoTransacao.RECEITA));
        }

        List<TransacaoFaturaDTO> resultado = new ArrayList<>(base);
        resultado.addAll(sinteticas);
        return resultado;
    }

    private boolean ehLinhaDePagamentoOuFinanciamento(String descricao) {
        String normalizada = normalizar(descricao);
        return normalizada.contains("pagamento em")
                || normalizada.contains("pagamento recebido")
                || normalizada.contains("pagamento efetuado")
                || normalizada.contains("saldo em atraso")
                || normalizada.contains("credito de atraso")
                || normalizada.contains("juros de atraso")
                || normalizada.contains("encerramento de divida")
                || normalizada.contains("iof de atraso")
                || normalizada.contains("multa de atraso")
                || normalizada.contains("juros de divida encerrada");
    }

    private boolean ehResumoDuplicado(String descricao) {
        String normalizada = normalizar(descricao);
        return normalizada.contains("fatura anterior")
                || normalizada.contains("total a pagar")
                || normalizada.contains("saldo em aberto")
                || normalizada.contains("proxima fatura")
                || normalizada.contains("limite");
    }

    private boolean temDescricao(List<TransacaoFaturaDTO> transacoes, String termo) {
        String alvo = normalizar(termo);
        return transacoes.stream()
                .map(TransacaoFaturaDTO::descricao)
                .filter(Objects::nonNull)
                .map(FaturaRegexService::normalizar)
                .anyMatch(descricao -> descricao.contains(alvo));
    }

    /**
     * Soma contábil das transações da fatura: despesas somam e receitas (estornos/créditos) abatem.
     */
    public static BigDecimal somar(List<TransacaoFaturaDTO> transacoes) {
        if (transacoes == null || transacoes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return transacoes.stream()
                .map(transacao -> {
                    BigDecimal v = transacao.valor();
                    if (v == null) {
                        return BigDecimal.ZERO;
                    }
                    return transacao.tipo() == TipoTransacao.RECEITA ? v.abs().negate() : v.abs();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private TransacaoFaturaDTO criarSintetica(String descricao, BigDecimal valor,
                                              LocalDate data, TipoTransacao tipo) {
        return new TransacaoFaturaDTO(descricao, valor.abs(), data, "Outros", tipo, null, null, null);
    }

    private Parcela extrairParcela(String descricao) {
        Matcher matcher = PARCELA.matcher(descricao);
        Parcela encontrada = null;
        while (matcher.find()) {
            try {
                int numero = Integer.parseInt(matcher.group(1));
                int total = Integer.parseInt(matcher.group(2));
                if (numero >= 1 && total >= 2 && numero <= total && total <= 60) {
                    encontrada = new Parcela(numero, total);
                }
            } catch (NumberFormatException ignored) {
                // segue para a próxima ocorrência
            }
        }
        return encontrada == null ? new Parcela(null, null) : encontrada;
    }

    public static String limparDescricao(String descricao) {
        if (descricao == null) {
            return "";
        }
        String limpa = PARCELA.matcher(descricao).replaceAll(" ");
        limpa = limpa.replaceAll("[()\\[\\]]", " ");
        limpa = limpa.replaceAll("[•·]+\\s*\\d{4}", " ");
        limpa = limpa.replaceAll("^\\s*\\d{2,3}\\.?\\d{3}\\.?\\d{3}", " ");
        limpa = limpa.replaceAll("\\s+", " ").trim();
        limpa = limpa.replaceAll("[-–—]+\\s*$", "").trim();
        return limpa;
    }

    // -------------------------------------------------------------------------
    // Categorização local
    // -------------------------------------------------------------------------

    public String classificarCategoria(String descricao) {
        String normalizada = normalizar(descricao);
        for (Map.Entry<String, List<String>> entrada : CATEGORIAS.entrySet()) {
            for (String palavra : entrada.getValue()) {
                if (normalizada.contains(palavra)) {
                    return entrada.getKey();
                }
            }
        }
        return "Outros";
    }

    // -------------------------------------------------------------------------
    // Conversões
    // -------------------------------------------------------------------------

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }


    /**
     * Verifica se a descrição/linha representa um crédito (estorno, devolução, reembolso etc.).
     */
    public static boolean indicaReceita(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        String normalizado = normalizar(texto);
        if (normalizado.contains("estorno") || normalizado.contains("devolucao")) {
            return true;
        }
        if (INDICADORES_DESPESA_FORCADA.stream().anyMatch(normalizado::contains)) {
            return false;
        }
        return INDICADORES_RECEITA.stream().anyMatch(normalizado::contains);
    }

    public static BigDecimal parseValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpo = valor.replace("R$", "").replace("\u00A0", "").trim();
        boolean negativo = false;

        // Formato contábil: (123,45) representa valor negativo.
        if (limpo.startsWith("(") && limpo.endsWith(")")) {
            negativo = true;
            limpo = limpo.substring(1, limpo.length() - 1).trim();
        }

        // Sinal negativo no início ou no fim: -123,45 ou 123,45-.
        if (limpo.startsWith("-") || limpo.startsWith("−")) {
            negativo = true;
            limpo = limpo.substring(1).trim();
        } else if (limpo.endsWith("-") || limpo.endsWith("−")) {
            negativo = true;
            limpo = limpo.substring(0, limpo.length() - 1).trim();
        } else {
            // Alguns emissores usam sufixo C (crédito) e D (débito).
            String maiusculo = limpo.toUpperCase(Locale.ROOT);
            if (maiusculo.endsWith("C")) {
                negativo = true;
                limpo = limpo.substring(0, limpo.length() - 1).trim();
            } else if (maiusculo.endsWith("D")) {
                limpo = limpo.substring(0, limpo.length() - 1).trim();
            }
        }

        limpo = limpo.replaceAll("[^0-9,.]", "");
        if (limpo.isBlank()) {
            return null;
        }
        if (limpo.contains(",") && limpo.contains(".")) {
            // O separador que aparece por último é o decimal: 1.234,56 (BR) ou 1,234.56 (US).
            if (limpo.lastIndexOf(',') > limpo.lastIndexOf('.')) {
                limpo = limpo.replace(".", "").replace(",", ".");
            } else {
                limpo = limpo.replace(",", "");
            }
        } else if (limpo.contains(",")) {
            limpo = limpo.replace(",", ".");
        } else if (limpo.contains(".")) {
            int ultimoPonto = limpo.lastIndexOf('.');
            if (limpo.length() - ultimoPonto - 1 != 2) {
                limpo = limpo.replace(".", "");
            }
        }
        try {
            BigDecimal numero = new BigDecimal(limpo);
            return negativo ? numero.negate() : numero;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public LocalDate parseData(String valor, String formato) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String data = valor.trim().replaceAll("\\s+", " ");

        if (formato != null && !formato.isBlank()) {
            try {
                return LocalDate.parse(data, DateTimeFormatter.ofPattern(formato, LOCALE_PT));
            } catch (DateTimeParseException ignored) {
                // tenta os formatos conhecidos abaixo
            }
        }

        Matcher iso = DATA_ISO.matcher(data);
        if (iso.matches()) {
            return criarData(Integer.parseInt(iso.group(1)), Integer.parseInt(iso.group(2)), Integer.parseInt(iso.group(3)));
        }

        Matcher br = DATA_BR.matcher(data);
        if (br.matches()) {
            int dia = Integer.parseInt(br.group(1));
            int mes = Integer.parseInt(br.group(2));
            int ano = br.group(3) == null ? LocalDate.now().getYear() : ajustarAno(Integer.parseInt(br.group(3)));
            return criarData(ano, mes, dia);
        }

        // Aceita datas por extenso como "12 de out. 2025" ou "12 de outubro de 2025".
        String dataSemPreposicao = data.replaceAll("(?i)\\s+de\\s+", " ");
        Matcher mes = DATA_MES.matcher(dataSemPreposicao);
        if (mes.matches()) {
            int dia = Integer.parseInt(mes.group(1));
            String nomeMes = normalizar(mes.group(2)).toUpperCase(Locale.ROOT).replace(".", "");
            Integer numeroMes = MESES.get(nomeMes);
            if (numeroMes != null) {
                int ano = mes.group(3) == null ? anoProvavelParaMes(numeroMes) : ajustarAno(Integer.parseInt(mes.group(3)));
                return criarData(ano, numeroMes, dia);
            }
        }

        for (String formatoConhecido : FORMATOS_DATA) {
            try {
                return LocalDate.parse(data, DateTimeFormatter.ofPattern(formatoConhecido, LOCALE_PT));
            } catch (DateTimeParseException ignored) {
                // continua
            }
        }
        return null;
    }

    private int anoProvavelParaMes(int mes) {
        LocalDate hoje = LocalDate.now();
        return mes > hoje.getMonthValue() + 1 ? hoje.getYear() - 1 : hoje.getYear();
    }

    private int ajustarAno(int ano) {
        return ano < 100 ? 2000 + ano : ano;
    }

    private LocalDate criarData(int ano, int mes, int dia) {
        try {
            return LocalDate.of(ano, mes, dia);
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Timeout de regex
    // -------------------------------------------------------------------------

    private <T> Optional<T> executarComTimeout(Callable<T> tarefa) {
        Future<T> future = REGEX_EXECUTOR.submit(tarefa);
        try {
            return Optional.ofNullable(future.get(timeoutMs, TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("Regex de fatura excedeu o tempo limite de {} ms e foi interrompido.", timeoutMs);
            return Optional.empty();
        } catch (Exception e) {
            future.cancel(true);
            return Optional.empty();
        }
    }

    public record ResultadoRegex(List<TransacaoFaturaDTO> transacoes,
                                 PadraoFatura padrao,
                                 BigDecimal valorTotalFatura) {
    }

    private record Parcela(Integer numero, Integer total) {
    }

    public record ResumoFatura(BigDecimal faturaAnterior,
                               BigDecimal pagamentoRecebido,
                               BigDecimal totalCompras,
                               BigDecimal iofInternacional,
                               BigDecimal outrosLancamentos,
                               BigDecimal totalPagar) {
    }

    private record DataToken(int inicio, int fim, int dia, int mes) {
    }

    private record ValorToken(int inicio, int fim, BigDecimal valor, boolean negativo) {
    }

    private record Segmento(int dia, int mes, String descricao, List<ValorToken> valores) {
    }

    private record ParValorDesc(ValorToken valor, String descricao) {
    }
}
