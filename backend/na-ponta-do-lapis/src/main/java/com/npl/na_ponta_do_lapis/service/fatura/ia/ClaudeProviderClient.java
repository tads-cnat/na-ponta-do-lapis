package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

@Service
public class ClaudeProviderClient implements IaProviderClient {

    private final IaHttpClient httpClient;

    @Value("${fatura.ia.claude.model:claude-sonnet-4-5}")
    private String modelo;

    public ClaudeProviderClient(IaHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean suporta(ProvedorIa provedor) {
        return provedor == ProvedorIa.CLAUDE;
    }

    @Override
    public String completar(ProvedorIa provedor, String apiKey, String prompt) {
        Map<String, Object> corpo = Map.of(
                "model", modelo,
                "max_tokens", 8192,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(Map.of("type", "text", "text", prompt))
                ))
        );
        JsonNode resposta = httpClient.post(url(), headers(apiKey), corpo);
        return extrairTexto(resposta);
    }

    @Override
    public String completarComPdf(ProvedorIa provedor, String apiKey, String prompt, byte[] pdf) {
        String base64 = Base64.getEncoder().encodeToString(pdf);
        Map<String, Object> corpo = Map.of(
                "model", modelo,
                "max_tokens", 8192,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "document", "source", Map.of(
                                        "type", "base64",
                                        "media_type", "application/pdf",
                                        "data", base64
                                )),
                                Map.of("type", "text", "text", prompt)
                        )
                ))
        );
        JsonNode resposta = httpClient.post(url(), headers(apiKey), corpo);
        return extrairTexto(resposta);
    }

    private String url() {
        return "https://api.anthropic.com/v1/messages";
    }

    private Map<String, String> headers(String apiKey) {
        return Map.of(
                "x-api-key", apiKey,
                "anthropic-version", "2023-06-01"
        );
    }

    private String extrairTexto(JsonNode resposta) {
        String texto = resposta.path("content").path(0).path("text").asText(null);
        if (texto == null || texto.isBlank()) {
            throw FaturaAnaliseException.respostaIaInvalida("O Claude não retornou texto.");
        }
        return texto;
    }
}
