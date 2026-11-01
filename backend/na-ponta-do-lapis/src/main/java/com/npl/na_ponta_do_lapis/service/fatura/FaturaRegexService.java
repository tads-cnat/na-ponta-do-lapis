package com.npl.na_ponta_do_lapis.service.fatura;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            "pagamento de fatura",
            "pagamento recebido",
            "pagamento efetuado",
            "estorno de pagamento",
            "saldo anterior",
            "total da fatura",
            "total a pagar",
            "vencimento",
            "limite disponivel",
            "limite de credito",
            "proxima fatura"
    );

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
        try (PDDocument documento = Loader.loadPDF(pdf)) {
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
            List<TransacaoFaturaDTO> transacoes = aplicarPadrao(padrao, texto);
            if (!transacoes.isEmpty()) {
                return new ResultadoRegex(transacoes, padrao, extrairTotal(padrao, texto));
            }
        }
        return new ResultadoRegex(List.of(), null, null);
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

            Matcher matcher = pattern.matcher(texto);
            while (matcher.find()) {
                String descricao = matcher.group("descricao");
                String data = matcher.group("data");
                String valor = matcher.group("valor");
                String linha = matcher.group();

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
                transacoes.add(new TransacaoFaturaDTO(
                        descricaoLimpa,
                        valorNumerico.abs(),
                        dataNumerica,
                        classificarCategoria(descricaoLimpa),
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
        if (padrao == null || padrao.getRegexTotal() == null || padrao.getRegexTotal().isBlank()
                || texto == null || texto.isBlank()) {
            return null;
        }
        Optional<BigDecimal> total = executarComTimeout(() -> {
            Pattern pattern;
            try {
                pattern = Pattern.compile(padrao.getRegexTotal(), REGEX_FLAGS);
            } catch (PatternSyntaxException e) {
                return null;
            }
            Matcher matcher = pattern.matcher(texto);
            BigDecimal ultimo = null;
            while (matcher.find()) {
                if (matcher.groupCount() >= 1) {
                    BigDecimal valor = parseValor(matcher.group(1));
                    if (valor != null) {
                        ultimo = valor.abs();
                    }
                }
            }
            return ultimo;
        });
        return total.orElse(null);
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

        return !aplicarPadrao(padrao, texto).isEmpty();
    }

    private boolean ignorarLinha(String texto) {
        if (texto == null) {
            return true;
        }
        String normalizado = normalizar(texto);
        return LINHAS_IGNORADAS.stream().anyMatch(normalizado::contains);
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

    private String limparDescricao(String descricao) {
        String limpa = PARCELA.matcher(descricao).replaceAll(" ");
        limpa = limpa.replaceAll("[()\\[\\]]", " ");
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

    public static BigDecimal parseValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpo = valor.replace("R$", "").replace("\u00A0", "").trim();
        boolean negativo = limpo.startsWith("-");
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

        Matcher mes = DATA_MES.matcher(data);
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
}
