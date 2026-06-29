package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.service.gemini.FaturaInteligenteService;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/faturas")
@Tag(name = "Faturas", description = "Análise inteligente de faturas de cartão de crédito")
public class FaturaInteligenteController {

    private final FaturaInteligenteService faturaService;

    public FaturaInteligenteController(FaturaInteligenteService faturaService) {
        this.faturaService = faturaService;
    }

    @Operation(summary = "Extrair transações de uma fatura PDF")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transações extraídas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido ou formato não suportado"),
        @ApiResponse(responseCode = "502", description = "Erro ao processar resposta da inteligência artificial", content = @Content(schema = @Schema(example = "{\"error\": \"Falha ao processar resposta da IA\"}"))),
        @ApiResponse(responseCode = "503", description = "Serviço temporariamente indisponível (Gemini API)", content = @Content(schema = @Schema(example = "{\"error\": \"Serviço temporariamente indisponível\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno ao processar a fatura")
    })
    @PostMapping(value = "/analise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extrairTransacoes(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            if (arquivo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo vazio. Envie um PDF de fatura."));
            }
            List<TransacaoFaturaDTO> transacoes = faturaService.processarFaturaPdf(arquivo);
            return ResponseEntity.ok(transacoes);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("indisponível")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Serviço temporariamente indisponível. Tente novamente em alguns instantes."));
            }
            if (msg != null && msg.contains("Timeout")) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                        .body(Map.of("error", "Tempo limite excedido ao contactar o serviço de inteligência artificial."));
            }
            if (msg != null && msg.contains("Falha ao converter")) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Erro ao processar resposta da inteligência artificial. O PDF pode estar em um formato não esperado."));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao processar a fatura."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao processar a fatura."));
        }
    }
}
