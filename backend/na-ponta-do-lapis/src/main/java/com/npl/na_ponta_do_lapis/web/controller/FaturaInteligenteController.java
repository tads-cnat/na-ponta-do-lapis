package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.service.gemini.FaturaInteligenteService;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/faturas")
public class FaturaInteligenteController {

    private final FaturaInteligenteService faturaService;

    public FaturaInteligenteController(FaturaInteligenteService faturaService) {
        this.faturaService = faturaService;
    }

    @PostMapping(value = "/extrair", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<TransacaoFaturaDTO>> extrairTransacoes(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            // Chama o seu serviço que faz as duas requisições para o Gemini
            List<TransacaoFaturaDTO> transacoes = faturaService.processarFaturaPdf(arquivo);
            
            // Retorna o JSON puro para você visualizar no teste
            return ResponseEntity.ok(transacoes);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}