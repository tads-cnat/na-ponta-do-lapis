package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

/**
 * Cliente HTTP comum aos provedores de IA, com timeout, retry e tradução de erros.
 */
@Component
public class IaHttpClient {

    private static final Logger log = LoggerFactory.getLogger(IaHttpClient.class);
    private static final int MAX_TENTATIVAS = 2;
    private static final long BACKOFF_MS = 1200;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public IaHttpClient(RestClient.Builder builder,
                        ObjectMapper objectMapper,
                        @Value("${fatura.ia.connect-timeout-seconds:10}") long connectTimeoutSeconds,
                        @Value("${fatura.ia.read-timeout-seconds:90}") long readTimeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        this.restClient = builder.requestFactory(factory).build();
        this.objectMapper = objectMapper;
    }

    public JsonNode post(String url, Map<String, String> headers, Object body) {
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                String resposta = restClient.post()
                        .uri(url)
                        .headers(h -> headers.forEach(h::set))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);

                if (resposta == null || resposta.isBlank()) {
                    throw FaturaAnaliseException.respostaIaInvalida("Resposta vazia.");
                }
                return objectMapper.readTree(resposta);
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                if (status == 401 || status == 403) {
                    throw FaturaAnaliseException.chaveInvalida();
                }
                if (status == 429) {
                    throw FaturaAnaliseException.limiteIa();
                }
                if (status >= 500 && tentativa < MAX_TENTATIVAS) {
                    aguardar(tentativa);
                    continue;
                }
                if (status >= 500) {
                    throw FaturaAnaliseException.provedorIndisponivel();
                }
                log.warn("Provedor de IA respondeu HTTP {}: {}", status, resumo(e.getResponseBodyAsString()));
                throw FaturaAnaliseException.respostaIaInvalida("HTTP " + status + ".");
            } catch (ResourceAccessException e) {
                if (tentativa < MAX_TENTATIVAS) {
                    aguardar(tentativa);
                    continue;
                }
                log.warn("Timeout ao chamar provedor de IA: {}", e.getMessage());
                throw FaturaAnaliseException.timeout();
            } catch (FaturaAnaliseException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Falha ao interpretar resposta do provedor de IA: {}", e.getMessage());
                throw FaturaAnaliseException.respostaIaInvalida("");
            }
        }
        throw FaturaAnaliseException.provedorIndisponivel();
    }

    private void aguardar(int tentativa) {
        try {
            Thread.sleep(BACKOFF_MS * tentativa);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String resumo(String corpo) {
        if (corpo == null) {
            return "";
        }
        String limpo = corpo.replaceAll("\\s+", " ").trim();
        return limpo.length() > 300 ? limpo.substring(0, 300) + "..." : limpo;
    }
}
