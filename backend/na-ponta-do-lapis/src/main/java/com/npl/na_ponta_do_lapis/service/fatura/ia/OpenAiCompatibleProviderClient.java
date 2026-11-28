package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

@Service
public class OpenAiCompatibleProviderClient implements IaProviderClient {

    private final IaHttpClient httpClient;
    private final ObjectProvider<ConfiguracaoIaService> configuracaoIaProvider;

    @Value("${fatura.ia.openai.model:gpt-4.1-mini}")
    private String modeloOpenAi;

    @Value("${fatura.ia.deepseek.model:deepseek-chat}")
    private String modeloDeepSeek;

    @Value("${fatura.ia.groq.model:llama-3.3-70b-versatile}")
    private String modeloGroq;

    public OpenAiCompatibleProviderClient(IaHttpClient httpClient) {
        this(httpClient, null);
    }

    @Autowired
    public OpenAiCompatibleProviderClient(IaHttpClient httpClient, ObjectProvider<ConfiguracaoIaService> configuracaoIaProvider) {
        this.httpClient = httpClient;
        this.configuracaoIaProvider = configuracaoIaProvider;
    }

    @Override
    public boolean suporta(ProvedorIa provedor) {
        return provedor == ProvedorIa.OPENAI
                || provedor == ProvedorIa.DEEPSEEK
                || provedor == ProvedorIa.GROQ;
    }

    @Override
    public String completar(ProvedorIa provedor, String apiKey, String prompt) {
        return executar(provedor, apiKey, prompt, true);
    }

    @Override
    public String completarTexto(ProvedorIa provedor, String apiKey, String prompt) {
        return executar(provedor, apiKey, null, prompt, false, 8192);
    }

    @Override
    public String completarChat(ProvedorIa provedor, String apiKey, String systemPrompt, String userMessage) {
        return executar(provedor, apiKey, systemPrompt, userMessage, false, 1200);
    }

    private String executar(ProvedorIa provedor, String apiKey, String prompt, boolean exigirJson) {
        return executar(provedor, apiKey, null, prompt, exigirJson, 8192);
    }

    private String executar(ProvedorIa provedor, String apiKey, String systemPrompt, String userMessage,
                            boolean exigirJson, int maxTokens) {
        Map<String, Object> corpo = new java.util.LinkedHashMap<>();
        corpo.put("model", modelo(provedor));
        corpo.put("temperature", exigirJson ? 0 : 0.3);
        corpo.put("max_tokens", maxTokens);
        if (exigirJson && (provedor == ProvedorIa.OPENAI || provedor == ProvedorIa.DEEPSEEK)) {
            corpo.put("response_format", Map.of("type", "json_object"));
        }
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            corpo.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
            ));
        } else {
            corpo.put("messages", List.of(Map.of("role", "user", "content", userMessage)));
        }
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
        ConfiguracaoIaService cfg = configuracaoIaProvider != null ? configuracaoIaProvider.getIfAvailable() : null;
        if (cfg != null) {
            return cfg.obterModeloAtivo(provedor);
        }
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
