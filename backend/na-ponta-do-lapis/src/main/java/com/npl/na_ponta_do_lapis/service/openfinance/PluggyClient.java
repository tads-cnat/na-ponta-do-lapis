package com.npl.na_ponta_do_lapis.service.openfinance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Component
public class PluggyClient {

    private static final Logger log = LoggerFactory.getLogger(PluggyClient.class);
    private static final String BASE_URL = "https://api.pluggy.ai";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PluggyClient(RestClient.Builder builder, ObjectMapper objectMapper,
                        @Value("${pluggy.api.timeout-seconds:30}") long timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = builder.baseUrl(BASE_URL).requestFactory(factory).build();
        this.objectMapper = objectMapper;
    }

    public String autenticar(String clientId, String clientSecret) {
        try {
            Map<String, String> body = Map.of(
                    "clientId", clientId,
                    "clientSecret", clientSecret
            );

            String responseBody = restClient.post()
                    .uri("/auth")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode apiKeyNode = root.path("apiKey");
            if (apiKeyNode.isMissingNode() || apiKeyNode.asText().isBlank()) {
                throw new IllegalStateException("Resposta da Pluggy não continha a apiKey esperada");
            }
            return apiKeyNode.asText();
        } catch (RestClientResponseException e) {
            log.error("[PluggyClient] Falha ao autenticar na Pluggy: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new IllegalArgumentException("Credenciais da Pluggy inválidas. Verifique seu Client ID e Client Secret.");
            }
            throw new IllegalStateException("Erro ao comunicar com o serviço Open Finance da Pluggy. Tente novamente mais tarde.", e);
        } catch (Exception e) {
            log.error("[PluggyClient] Erro inesperado ao autenticar", e);
            throw new IllegalStateException("Erro ao conectar com a Pluggy: " + e.getMessage(), e);
        }
    }

    public String criarConnectToken(String apiKey) {
        try {
            String responseBody = restClient.post()
                    .uri("/connect_token")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode tokenNode = root.path("accessToken");
            if (tokenNode.isMissingNode() || tokenNode.asText().isBlank()) {
                throw new IllegalStateException("Resposta da Pluggy não continha o accessToken para o widget Connect");
            }
            return tokenNode.asText();
        } catch (Exception e) {
            log.error("[PluggyClient] Erro ao criar Connect Token na Pluggy", e);
            throw new IllegalStateException("Falha ao gerar sessão de conexão com a Pluggy: " + e.getMessage(), e);
        }
    }

    public JsonNode listarItems(String apiKey) {
        try {
            String responseBody = restClient.get()
                    .uri("/items")
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("results");
        } catch (Exception e) {
            log.error("[PluggyClient] Erro ao listar items/conexões", e);
            throw new IllegalStateException("Falha ao buscar conexões no Open Finance: " + e.getMessage(), e);
        }
    }

    public JsonNode buscarItem(String apiKey, String itemId) {
        try {
            String responseBody = restClient.get()
                    .uri("/items/" + itemId.trim())
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(responseBody);
        } catch (Exception e) {
            log.error("[PluggyClient] Erro ao buscar item itemId={}", itemId, e);
            throw new IllegalStateException("Falha ao consultar item na Pluggy: " + e.getMessage(), e);
        }
    }

    public JsonNode listarContasPorItem(String apiKey, String itemId) {
        try {
            String responseBody = restClient.get()
                    .uri("/accounts?itemId=" + itemId)
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("results");
        } catch (Exception e) {
            log.error("[PluggyClient] Erro ao listar contas para itemId={}", itemId, e);
            throw new IllegalStateException("Falha ao buscar contas no Open Finance: " + e.getMessage(), e);
        }
    }

    public JsonNode listarContas(String apiKey) {
        try {
            String responseBody = restClient.get()
                    .uri("/accounts")
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("results");
        } catch (Exception e) {
            log.error("[PluggyClient] Erro ao listar contas", e);
            throw new IllegalStateException("Falha ao buscar contas no Open Finance: " + e.getMessage(), e);
        }
    }

    public JsonNode listarTransacoes(String apiKey, String accountId, LocalDate de, LocalDate ate) {
        com.fasterxml.jackson.databind.node.ArrayNode todasTransacoes = objectMapper.createArrayNode();

        String proximaUri = "/v2/transactions?accountId=" + accountId;
        int paginasProcessadas = 0;
        final int MAX_PAGINAS = 10;

        while (proximaUri != null && !proximaUri.isBlank() && paginasProcessadas < MAX_PAGINAS) {
            try {
                String responseBody = restClient.get()
                        .uri(proximaUri)
                        .header("X-API-KEY", apiKey)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode results = root.path("results");
                if (results.isArray()) {
                    for (JsonNode tx : results) {
                        todasTransacoes.add(tx);
                    }
                }

                JsonNode nextNode = root.path("next");
                if (nextNode.isTextual() && !nextNode.asText().isBlank() && !nextNode.asText().equalsIgnoreCase("null")) {
                    String nextVal = nextNode.asText().trim();
                    String cursorToken = null;

                    if (nextVal.contains("after=")) {
                        int idx = nextVal.indexOf("after=");
                        cursorToken = nextVal.substring(idx + 6);
                        int endIdx = cursorToken.indexOf('&');
                        if (endIdx != -1) {
                            cursorToken = cursorToken.substring(0, endIdx);
                        }
                    } else if (nextVal.startsWith("?")) {
                        cursorToken = nextVal.substring(1);
                    } else {
                        cursorToken = nextVal;
                    }

                    if (cursorToken != null && !cursorToken.isBlank()) {
                        proximaUri = "/v2/transactions?accountId=" + accountId + "&after=" + cursorToken;
                        paginasProcessadas++;
                    } else {
                        proximaUri = null;
                    }
                } else {
                    proximaUri = null;
                }
            } catch (Exception e) {
                log.warn("[PluggyClient] Aviso ao buscar página de transações para accountId={}: {}", accountId, e.getMessage());
                break;
            }
        }

        // Se for conta de cartão de crédito e não retornou transações pelo endpoint direto, busca pelas faturas (/bills)
        if (todasTransacoes.isEmpty()) {
            try {
                log.info("[PluggyClient] Nenhuma transação direta para accountId={}. Verificando faturas (/bills)...", accountId);
                String billsBody = restClient.get()
                        .uri("/bills?accountId=" + accountId)
                        .header("X-API-KEY", apiKey)
                        .retrieve()
                        .body(String.class);

                JsonNode billsRoot = objectMapper.readTree(billsBody);
                JsonNode billsArray = billsRoot.path("results");
                if (billsArray.isArray()) {
                    for (JsonNode bill : billsArray) {
                        String billId = bill.path("id").asText();
                        if (billId != null && !billId.isBlank()) {
                            try {
                                String billTxBody = restClient.get()
                                        .uri("/bills/" + billId + "/transactions")
                                        .header("X-API-KEY", apiKey)
                                        .retrieve()
                                        .body(String.class);

                                JsonNode bRoot = objectMapper.readTree(billTxBody);
                                JsonNode bResults = bRoot.path("results");
                                if (bResults.isArray()) {
                                    for (JsonNode btx : bResults) {
                                        todasTransacoes.add(btx);
                                    }
                                }
                            } catch (Exception ex) {
                                log.debug("[PluggyClient] Erro ao consultar transações da fatura {}: {}", billId, ex.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("[PluggyClient] Consulta a /bills não retornou dados para accountId={}: {}", accountId, e.getMessage());
            }
        }

        log.info("[PluggyClient] Total de transações recuperadas para accountId={}: {}", accountId, todasTransacoes.size());
        return todasTransacoes;
    }

    public JsonNode listarFaturasPorConta(String apiKey, String accountId) {
        try {
            String billsBody = restClient.get()
                    .uri("/bills?accountId=" + accountId)
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .body(String.class);

            JsonNode billsRoot = objectMapper.readTree(billsBody);
            return billsRoot.path("results");
        } catch (Exception e) {
            log.warn("[PluggyClient] Erro ao buscar /bills para accountId={}: {}", accountId, e.getMessage());
            return null;
        }
    }
}

