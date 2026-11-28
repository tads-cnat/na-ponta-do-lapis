package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

@Service
public class GeminiProviderClient implements IaProviderClient {

    private final IaHttpClient httpClient;
    private final ObjectProvider<ConfiguracaoIaService> configuracaoIaProvider;

    @Value("${fatura.ia.gemini.model:gemini-2.5-flash}")
    private String modelo;

    public GeminiProviderClient(IaHttpClient httpClient) {
        this(httpClient, null);
    }

    @Autowired
    public GeminiProviderClient(IaHttpClient httpClient, ObjectProvider<ConfiguracaoIaService> configuracaoIaProvider) {
        this.httpClient = httpClient;
        this.configuracaoIaProvider = configuracaoIaProvider;
    }

    private String modeloAtivo() {
        ConfiguracaoIaService cfg = configuracaoIaProvider != null ? configuracaoIaProvider.getIfAvailable() : null;
        return cfg != null ? cfg.obterModeloAtivo(ProvedorIa.GEMINI) : modelo;
    }

    @Override
    public boolean suporta(ProvedorIa provedor) {
        return provedor == ProvedorIa.GEMINI;
    }

    @Override
    public String completar(ProvedorIa provedor, String apiKey, String prompt) {
        Map<String, Object> corpo = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", configuracaoGeracao(true)
        );
        JsonNode resposta = httpClient.post(url(), headers(apiKey), corpo);
        return extrairTexto(resposta);
    }

    @Override
    public String completarTexto(ProvedorIa provedor, String apiKey, String prompt) {
        Map<String, Object> corpo = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", configuracaoGeracao(false, 8192)
        );
        JsonNode resposta = httpClient.post(url(), headers(apiKey), corpo);
        return extrairTexto(resposta);
    }

    @Override
    public String completarChat(ProvedorIa provedor, String apiKey, String systemPrompt, String userMessage) {
        Map<String, Object> corpo = new java.util.LinkedHashMap<>();
        corpo.put("systemInstruction", Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
        ));
        corpo.put("contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userMessage))
        )));
        corpo.put("generationConfig", configuracaoGeracao(false, 1200));
        JsonNode resposta = httpClient.post(url(), headers(apiKey), corpo);
        return extrairTexto(resposta);
    }

    @Override
    public String completarComPdf(ProvedorIa provedor, String apiKey, String prompt, byte[] pdf) {
        String base64 = Base64.getEncoder().encodeToString(pdf);
        Map<String, Object> corpo = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("inlineData", Map.of(
                                        "mimeType", "application/pdf",
                                        "data", base64
                                )),
                                Map.of("text", prompt)
                        )
                )),
                "generationConfig", configuracaoGeracao(true, 8192)
        );
        JsonNode resposta = httpClient.post(url(), headers(apiKey), corpo);
        return extrairTexto(resposta);
    }

    private Map<String, Object> configuracaoGeracao(boolean exigirJson) {
        return configuracaoGeracao(exigirJson, 8192);
    }

    /**
     * Extração não precisa de criatividade. Desligar o "thinking" evita gastar
     * o limite de saída e truncar o JSON em faturas grandes.
     */
    private Map<String, Object> configuracaoGeracao(boolean exigirJson, int maxTokens) {
        Map<String, Object> configuracao = new java.util.LinkedHashMap<>();
        if (exigirJson) {
            configuracao.put("responseMimeType", "application/json");
            configuracao.put("temperature", 0);
        } else {
            configuracao.put("temperature", 0.3);
        }
        configuracao.put("maxOutputTokens", maxTokens);
        String atual = modeloAtivo();
        // thinkingBudget=0 só é suportado pelo Gemini 2.5 Flash (Gemini 2.0 e 3.x rejeitam com HTTP 400).
        if (atual != null && atual.toLowerCase(java.util.Locale.ROOT).contains("2.5-flash")) {
            configuracao.put("thinkingConfig", Map.of("thinkingBudget", 0));
        }
        return configuracao;
    }

    private String url() {
        return "https://generativelanguage.googleapis.com/v1beta/models/"
                + modeloAtivo() + ":generateContent";
    }

    // A chave vai no cabeçalho (e não na query string) para não vazar em logs,
    // proxies ou mensagens de exceção do cliente HTTP.
    private Map<String, String> headers(String apiKey) {
        return Map.of("x-goog-api-key", apiKey);
    }

    private String extrairTexto(JsonNode resposta) {
        JsonNode parts = resposta.path("candidates").path(0)
                .path("content").path("parts");
        if (parts.isArray()) {
            StringBuilder texto = new StringBuilder();
            for (JsonNode part : parts) {
                texto.append(part.path("text").asText(""));
            }
            if (!texto.isEmpty()) {
                return texto.toString();
            }
        }
        String texto = parts.path(0).path("text").asText(null);
        if (texto == null || texto.isBlank()) {
            throw FaturaAnaliseException.respostaIaInvalida("O Gemini não retornou texto.");
        }
        return texto;
    }
}
