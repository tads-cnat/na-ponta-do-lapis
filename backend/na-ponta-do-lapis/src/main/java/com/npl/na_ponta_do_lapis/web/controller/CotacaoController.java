package com.npl.na_ponta_do_lapis.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.npl.na_ponta_do_lapis.service.CotacaoService;
import com.npl.na_ponta_do_lapis.web.dto.CotacaoResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/cotacoes")
@Tag(name = "Cotações", description = "Consulta de cotação atual de moedas via AwesomeAPI")
public class CotacaoController {

    private final CotacaoService cotacaoService;

    public CotacaoController(CotacaoService cotacaoService) {
        this.cotacaoService = cotacaoService;
    }

    @Operation(summary = "Buscar cotação atual de um par de moedas")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{moeda}")
    public ResponseEntity<CotacaoResponseDTO> buscarCotacaoAtual(@PathVariable String moeda) {
        return ResponseEntity.status(HttpStatus.OK).body(cotacaoService.buscarCotacaoAtual(moeda));
    }
}