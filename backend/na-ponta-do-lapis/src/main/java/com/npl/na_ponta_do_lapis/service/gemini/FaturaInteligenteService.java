package com.npl.na_ponta_do_lapis.service.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

import java.io.IOException;
import java.util.List;

@Service
public class FaturaInteligenteService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper; 

    public FaturaInteligenteService(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public List<TransacaoFaturaDTO> processarFaturaPdf(MultipartFile arquivoPdf) throws IOException {
        String fileUri = fazerUploadParaGemini(arquivoPdf);

        return extrairTransacoes(fileUri);
    }

    private String fazerUploadParaGemini(MultipartFile pdf) throws IOException {
        String uploadUrl = "https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + apiKey;

    
        String respostaUpload = restClient.post()
                .uri(uploadUrl)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(pdf.getBytes())
                .retrieve()
                .body(String.class);

        JsonNode jsonNode = objectMapper.readTree(respostaUpload);
        return jsonNode.path("file").path("uri").asText(); 
    }

    private List<TransacaoFaturaDTO> extrairTransacoes(String fileUri) {
        String generateUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;

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

        String respostaIa = restClient.post()
                .uri(generateUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadJson)
                .retrieve()
                .body(String.class);

        try {
            // 1. Lê o envelope da IA
            JsonNode rootNode = objectMapper.readTree(respostaIa);
            
            // 2. Navega até o array de texto bruto
            String jsonPuro = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            // 3. Remove eventuais sujeiras de marcação (```json e ```)
            jsonPuro = jsonPuro.replaceAll("^```json\\s*", "").replaceAll("```$", "").trim();

            // 4. Mapeia a string limpa para os seus objetos Java
            return objectMapper.readValue(
                    jsonPuro,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TransacaoFaturaDTO.class));

        } catch (Exception e) {
            throw new RuntimeException("Falha ao converter a resposta da IA para a lista de transações: " + e.getMessage(), e);
        } 
    }
}