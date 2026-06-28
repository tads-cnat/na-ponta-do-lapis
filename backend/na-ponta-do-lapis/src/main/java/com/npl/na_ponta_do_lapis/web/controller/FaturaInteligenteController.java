package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.service.gemini.FaturaInteligenteService;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
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
        @ApiResponse(responseCode = "500", description = "Erro interno ao processar a fatura")
    })
    @PostMapping(value = "/analise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<TransacaoFaturaDTO>> extrairTransacoes(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            List<TransacaoFaturaDTO> transacoes = faturaService.processarFaturaPdf(arquivo);
            return ResponseEntity.ok(transacoes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}