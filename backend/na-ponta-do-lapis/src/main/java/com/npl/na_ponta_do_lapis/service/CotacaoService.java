package com.npl.na_ponta_do_lapis.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.web.dto.CotacaoApiDTO;
import com.npl.na_ponta_do_lapis.web.dto.CotacaoResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.ApiExternaCotacaoException;

@Service
public class CotacaoService {

    private static final String BASE_URL = "https://economia.awesomeapi.com.br";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CotacaoService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Busca a cotação atual de um par de moedas direto na AwesomeAPI.
     * Não salva nada — só repassa o dado pra tela.
     *
     * @param moeda Par de moedas. Ex.: "USD-BRL"
     */
    public CotacaoResponseDTO buscarCotacaoAtual(String moeda) {
        String url = BASE_URL + "/json/last/" + moeda;

        // A AwesomeAPI devolve um objeto com chave dinâmica (ex.: "USDBRL"),
        // então pegamos só o primeiro valor da resposta.
        List<CotacaoApiDTO> resposta = chamarApi(url);

        if (resposta.isEmpty()) {
            throw new ApiExternaCotacaoException("AwesomeAPI não retornou cotação para: " + moeda);
        }

        return new CotacaoResponseDTO(resposta.get(0));
    }

    private List<CotacaoApiDTO> chamarApi(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiExternaCotacaoException(
                        "AwesomeAPI retornou status " + response.statusCode() + " para: " + url
                );
            }

            // O endpoint /json/last/{moeda} devolve um OBJETO (não array),
            // com chave dinâmica tipo {"USDBRL": {...}}. Por isso lemos como
            // árvore genérica e extraímos só os valores.
            var arvore = objectMapper.readTree(response.body());
            return objectMapper.convertValue(
                    arvore,
                    new TypeReference<java.util.Map<String, CotacaoApiDTO>>() {}
            ).values().stream().toList();

        } catch (ApiExternaCotacaoException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiExternaCotacaoException("Falha ao consultar a AwesomeAPI: " + e.getMessage());
        }
    }
}