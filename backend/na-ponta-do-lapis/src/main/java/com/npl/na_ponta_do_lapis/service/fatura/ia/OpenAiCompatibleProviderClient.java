package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

@Service
public class OpenAiCompatibleProviderClient implements IaProviderClient {

    private final IaHttpClient httpClient;

    @Value("${fatura.ia.openai.model:gpt-4.1-mini}")
    private String modeloOpenAi;

    @Value("${fatura.ia.deepseek.model:deepseek-chat}")
    private String modeloDeepSeek;

    @Value("${fatura.ia.groq.model:llama-3.3-70b-versatile}")
    private String modeloGroq;

    public OpenAiCompatibleProviderClient(IaHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean suporta(ProvedorIa provedor) {
        return provedor == ProvedorIa.OPENAI
                || provedor == ProvedorIa.DEEPSEEK
                || provedor == ProvedorIa.GROQ;
    }

    @Override
    public String completar(ProvedorIa provedor, String apiKey, String prompt) {
        Map<String, Object> corpo = Map.of(
                "model", modelo(provedor),
                "temperature", 0,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
        Map<String, String> headers = Map.of("Authorization", "Bearer " + apiKey);
        JsonNode resposta = httpClient.post(url(provedor), headers, corpo);
        return extrairTexto(resposta);
    }

    private String url(ProvedorIa provedor) {
        return switch (provedor) {
            case OPENAI -> "https://api.openai.com/v1/chat/completions";
            case DEEPSEEK -> "https://api.deepseek.com/chat/completions";
            case GROQ -> "https://api.groq.com/openai/v1/chat/completions";
            default -> throw FaturaAnaliseException.provedorSemSuportePdf();
        };
    }

    private String modelo(ProvedorIa provedor) {
        return switch (provedor) {
            case OPENAI -> modeloOpenAi;
            case DEEPSEEK -> modeloDeepSeek;
            case GROQ -> modeloGroq;
            default -> throw FaturaAnaliseException.provedorSemSuportePdf();
        };
    }

    private String extrairTexto(JsonNode resposta) {
        JsonNode conteudo = resposta.path("choices").path(0).path("message").path("content");
        if (conteudo.isArray()) {
            StringBuilder texto = new StringBuilder();
            conteudo.forEach(parte -> texto.append(parte.path("text").asText("")));
            if (!texto.isEmpty()) {
                return texto.toString();
            }
        }
        String texto = conteudo.asText(null);
        if (texto == null || texto.isBlank()) {
            throw FaturaAnaliseException.respostaIaInvalida("O provedor não retornou texto.");
        }
        return texto;
    }
}
