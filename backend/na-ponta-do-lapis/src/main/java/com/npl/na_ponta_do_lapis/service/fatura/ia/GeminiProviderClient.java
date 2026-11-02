package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

@Service
public class GeminiProviderClient implements IaProviderClient {

    private final IaHttpClient httpClient;

    @Value("${fatura.ia.gemini.model:gemini-2.5-flash}")
    private String modelo;

    public GeminiProviderClient(IaHttpClient httpClient) {
        this.httpClient = httpClient;
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
                "generationConfig", configuracaoGeracao()
        );
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
                "generationConfig", configuracaoGeracao()
        );
        JsonNode resposta = httpClient.post(url(), headers(apiKey), corpo);
        return extrairTexto(resposta);
    }

    /**
     * Extração não precisa de criatividade. Desligar o "thinking" evita gastar
     * o limite de saída e truncar o JSON em faturas grandes.
     */
    private Map<String, Object> configuracaoGeracao() {
        Map<String, Object> configuracao = new java.util.LinkedHashMap<>();
        configuracao.put("responseMimeType", "application/json");
        configuracao.put("temperature", 0);
        configuracao.put("maxOutputTokens", 8192);
        // O orçamento zero de "thinking" só é aceito pelos modelos Flash.
        if (modelo != null && modelo.toLowerCase(java.util.Locale.ROOT).contains("flash")) {
            configuracao.put("thinkingConfig", Map.of("thinkingBudget", 0));
        }
        return configuracao;
    }

    private String url() {
        return "https://generativelanguage.googleapis.com/v1beta/models/"
                + modelo + ":generateContent";
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
