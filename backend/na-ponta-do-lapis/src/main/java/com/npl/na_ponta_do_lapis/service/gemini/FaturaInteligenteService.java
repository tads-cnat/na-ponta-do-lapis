package com.npl.na_ponta_do_lapis.service.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

import java.io.IOException;
import java.util.List;

@Service
public class FaturaInteligenteService {

    private static final int MAX_TENTATIVAS = 3;
    private static final long BACKOFF_MS = 2000;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public FaturaInteligenteService(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public List<TransacaoFaturaDTO> processarFaturaPdf(MultipartFile arquivoPdf) throws IOException {
        String fileUri = fazerUploadParaGemini(arquivoPdf, 1);
        return extrairTransacoes(fileUri, 1);
    }

    private String fazerUploadParaGemini(MultipartFile pdf, int tentativa) throws IOException {
        String uploadUrl = "https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + apiKey;
        try {
            String respostaUpload = restClient.post()
                    .uri(uploadUrl)
                    .contentType(MediaType.parseMediaType("application/pdf"))
                    .body(pdf.getBytes())
                    .retrieve()
                    .body(String.class);

            JsonNode jsonNode = objectMapper.readTree(respostaUpload);
            return jsonNode.path("file").path("uri").asText();
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            if (tentativa < MAX_TENTATIVAS) {
                esperar(tentativa);
                return fazerUploadParaGemini(pdf, tentativa + 1);
            }
            throw new RuntimeException("Gemini API indisponível após " + MAX_TENTATIVAS + " tentativas");
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Timeout de conexão com Gemini API durante upload");
        }
    }

    private List<TransacaoFaturaDTO> extrairTransacoes(String fileUri, int tentativa) {
        String generateUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String payloadJson = """
            {
              "contents": [{
                "parts": [
                  {
                    "fileData": {
                      "mimeType": "application/pdf",
                      "fileUri": "%s"
                    }
                  },
                  {
                    "text": "Analise esta fatura de cartão de crédito. Retorne EXCLUSIVAMENTE um array JSON contendo as transações. Cada objeto deve ter os campos: 'descricao' (string), 'valor' (numero decimal), 'data' (formato YYYY-MM-DD) e 'categoria' (string). REGRA CRÍTICA PARA A CATEGORIA: Você deve deduzir a categoria baseada no nome do estabelecimento. Você SÓ PODE escolher UMA destas opções exatas: 'Alimentação', 'Transporte', 'Saúde', 'Educação', 'Lazer'. Se não conseguir deduzir ou não se encaixar em nenhuma destas, use obrigatoriamente a opção 'Outros'. Não inclua formatação markdown."
                  }
                ]
              }]
            }
            """.formatted(fileUri);

        try {
            String respostaIa = restClient.post()
                    .uri(generateUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payloadJson)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(respostaIa);

            String jsonPuro = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            jsonPuro = jsonPuro.replaceAll("^```json\\s*", "").replaceAll("```$", "").trim();

            return objectMapper.readValue(
                    jsonPuro,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TransacaoFaturaDTO.class));

        } catch (HttpServerErrorException.ServiceUnavailable e) {
            if (tentativa < MAX_TENTATIVAS) {
                esperar(tentativa);
                return extrairTransacoes(fileUri, tentativa + 1);
            }
            throw new RuntimeException("Gemini API indisponível após " + MAX_TENTATIVAS + " tentativas");
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Timeout de conexão com Gemini API durante análise");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao converter a resposta da IA para a lista de transações: " + e.getMessage(), e);
        }
    }

    private void esperar(int tentativa) {
        try {
            Thread.sleep(BACKOFF_MS * tentativa);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
