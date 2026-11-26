package com.npl.na_ponta_do_lapis.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Camada determinística de proteção (custo zero de IA) para o Consultor Grafith:
 * 1. Bloqueia tentativas explícitas de prompt injection / jailbreak / vazamento de regras.
 * 2. Recusa pedidos claramente fora de escopo quando não possuem relação com finanças pessoais.
 * 3. Sanitiza o histórico enviado pelo cliente contra history poisoning.
 */
@Component
public class ConsultorGuardrail {

    public static final String RESPOSTA_FORA_ESCOPO =
            "Oi! Eu sou o **Grafith**, seu consultor financeiro aqui no Na Ponta do Lápis. "
            + "Só consigo ajudar com assuntos ligados às suas **finanças pessoais e à sua conta** "
            + "(transações, saldos, gastos por categoria, metas, assinaturas e parcelamentos).\n\n"
            + "Que tal me perguntar algo como: *\"Onde gastei mais este mês?\"* ou *\"Como estão minhas metas?\"*";

    public static final String RESPOSTA_INJECTION =
            "Como consultor financeiro do Na Ponta do Lápis, sigo regras fixas de segurança e privacidade "
            + "e só posso analisar seus dados financeiros em modo somente leitura. "
            + "Em que posso ajudar nas suas contas, gastos, metas ou assinaturas?";

    private static final List<Pattern> PADROES_INJECTION = List.of(
            Pattern.compile("\\b(ignore|ignorar|desconsidere|esqueca|esquece)\\b.{0,35}\\b(instrucoes|regras|comandos|prompt|sistema|anteriores|tudo)\\b"),
            Pattern.compile("\\b(ignore|disregard|forget)\\b.{0,30}\\b(previous|all|above|system)\\b.{0,20}\\b(instructions|prompts|rules)\\b"),
            Pattern.compile("\\b(system\\s*prompt|prompt\\s+do\\s+sistema|regras\\s+internas|instrucoes\\s+internas|reveal\\s+your\\s+prompt|mostre\\s+seu\\s+prompt|imprima\\s+o\\s+json)\\b"),
            Pattern.compile("\\b(jailbreak|modo\\s+desenvolvedor|developer\\s+mode|dan\\s+mode|bypass)\\b"),
            Pattern.compile("\\b(finja\\s+que|finja\\s+ser|aja\\s+como|atue\\s+como|voce\\s+agora\\s+e|agora\\s+voce\\s+e|mude\\s+sua\\s+persona)\\b")
    );

    private static final List<Pattern> PADROES_FORA_ESCOPO = List.of(
            Pattern.compile("\\b(receita\\s+de|como\\s+fazer\\s+bolo|como\\s+cozinhar|ingredientes\\s+para\\s+fazer)\\b"),
            Pattern.compile("\\b(escreva\\s+um\\s+codigo|crie\\s+um\\s+codigo|faca\\s+um\\s+codigo|codigo\\s+em\\s+(python|java|javascript|typescript|c\\+\\+|php|sql|html)|debugar\\s+codigo|algoritmo\\s+em)\\b"),
            Pattern.compile("\\b(escreva\\s+um\\s+(poema|conto|musica|redacao|discurso|email\\s+de\\s+amor)|conte\\s+uma\\s+piada|piada\\s+sobre)\\b"),
            Pattern.compile("\\b(quem\\s+ganhou\\s+o\\s+jogo|placar\\s+do\\s+jogo|escalacao\\s+do|campeonato\\s+brasileiro|copa\\s+do\\s+mundo)\\b"),
            Pattern.compile("\\b(diagnostico\\s+medico|qual\\s+remedio\\s+tomar|prescreva\\s+um\\s+remedio|sintomas\\s+de\\s+doenca)\\b"),
            Pattern.compile("\\b(traduza\\s+(este|esse|o)\\s+texto|resuma\\s+o\\s+(livro|filme|serie))\\b")
    );

    private static final Pattern TERMOS_FINANCEIROS = Pattern.compile(
            "\\b(gasto|gastos|gastei|despesa|despesas|receita|receitas|saldo|saldos|conta|contas|transacao|transacoes|"
            + "meta|metas|assinatura|assinaturas|parcela|parcelas|parcelamento|parcelamentos|fatura|faturas|cartao|cartoes|"
            + "categoria|categorias|orcamento|dinheiro|economizar|economia|poupanca|divida|dividas|aporte|aportes|pagar|paguei|renda|salario|financeiro|financeira|financas)\\b"
    );

    /**
     * Retorna uma resposta pronta de recusa se a pergunta violar o guardrail,
     * ou Optional.empty() se puder seguir para o modelo de IA.
     */
    public Optional<String> validarPergunta(String pergunta) {
        if (pergunta == null || pergunta.isBlank()) {
            return Optional.of(RESPOSTA_FORA_ESCOPO);
        }

        String normalizada = normalizar(pergunta);

        for (Pattern padrao : PADROES_INJECTION) {
            if (padrao.matcher(normalizada).find()) {
                return Optional.of(RESPOSTA_INJECTION);
            }
        }

        boolean mencionaFinancas = TERMOS_FINANCEIROS.matcher(normalizada).find();
        if (!mencionaFinancas) {
            for (Pattern padrao : PADROES_FORA_ESCOPO) {
                if (padrao.matcher(normalizada).find()) {
                    return Optional.of(RESPOSTA_FORA_ESCOPO);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Remove delimitadores XML/tags e limita o tamanho de entradas de histórico
     * vindas do cliente para evitar history poisoning.
     */
    public String sanitizarTextoUsuario(String texto, int maxChars) {
        if (texto == null) {
            return "";
        }
        String limpo = texto
                .replace("<", "(")
                .replace(">", ")")
                .trim();
        if (limpo.length() > maxChars) {
            return limpo.substring(0, maxChars) + "...";
        }
        return limpo;
    }

    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
