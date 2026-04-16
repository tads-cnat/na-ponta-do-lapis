package com.npl.na_ponta_do_lapis.web.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.npl.na_ponta_do_lapis.service.ContaFinanceiraService;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/conta")
@Tag(name = "Contas", description = "Gerenciamento de Contas Financeiras")
public class ContaFinanceiraController {

    private ContaFinanceiraService contaService;

    public ContaFinanceiraController(ContaFinanceiraService contaService) {
        this.contaService = contaService;
    }

    @Operation(summary = "Criar conta")
    @PostMapping
    public ResponseEntity<ContaFinanceiraResponseDTO> criarConta(@RequestBody ContaFinanceiraDTO contaDTO){
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(contaService.criarConta(contaDTO));
    }

    @Operation(summary = "Listar contas")
    @GetMapping
    public ResponseEntity<List<ContaFinanceiraResponseDTO>> listarTodos(){
        return ResponseEntity.status(HttpStatus.OK).body(contaService.listarContas());
    
    }

    @Operation(summary = "Buscar conta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ContaFinanceiraResponseDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(contaService.buscarContaPorId(id));
    }

    @Operation(summary = "Atualizar conta")
    @PutMapping("/{id}")
    public ResponseEntity<ContaFinanceiraResponseDTO> atualizarConta(
        @PathVariable Long id,
        @RequestBody ContaFinanceiraDTO contaDTO){
        return ResponseEntity.status(HttpStatus.OK).body(contaService.atualizarConta(id, contaDTO));
    }

    @Operation(summary = "Apagar conta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirConta(@PathVariable Long id){
        contaService.excluirConta(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}