package com.npl.na_ponta_do_lapis.web.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.npl.na_ponta_do_lapis.service.ContaFinanceiraService;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraPatchDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraResponseDTO;
import com.npl.na_ponta_do_lapis.web.validator.ContaFinanceiraPatchValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas", description = "Gerenciamento de Contas Financeiras")
public class ContaFinanceiraController {


    private final ContaFinanceiraService contaService;
    private final ContaFinanceiraPatchValidator patchValidator;

    public ContaFinanceiraController(ContaFinanceiraService contaService, ContaFinanceiraPatchValidator patchValidator) {
        this.contaService = contaService;
        this.patchValidator = patchValidator;
    }

    @Operation(summary = "Criar conta")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PostMapping
    public ResponseEntity<ContaFinanceiraResponseDTO> criarConta(@Valid @RequestBody ContaFinanceiraDTO contaDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(contaService.criarConta(contaDTO));
    }

    @Operation(summary = "Listar contas")
    @GetMapping
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<List<ContaFinanceiraResponseDTO>> listarTodos(){
        return ResponseEntity.status(HttpStatus.OK).body(contaService.listarContas());
    }

    @Operation(summary = "Buscar conta por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<ContaFinanceiraResponseDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(contaService.buscarContaPorId(id));
    }

    @Operation(summary = "Atualizar conta")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PutMapping("/{id}")
    public ResponseEntity<ContaFinanceiraResponseDTO> atualizarConta(@PathVariable Long id, @Valid @RequestBody ContaFinanceiraDTO contaDTO){
        return ResponseEntity.status(HttpStatus.OK).body(contaService.atualizarConta(id, contaDTO));
    }

    @Operation(summary = "Listar contas do usuário logado")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<List<ContaFinanceiraResponseDTO>> minhasContasFinanceiras(){
        List<ContaFinanceiraResponseDTO> minhasContas = contaService.listarContaFinanceiraUsuarioLogado().stream()
                .map(c -> new ContaFinanceiraResponseDTO(c))
                .toList();
        return ResponseEntity.ok(minhasContas);
    }

    @Operation(summary = "Atualizar parte da conta")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PatchMapping("/{id}")
    public ResponseEntity<ContaFinanceiraResponseDTO> atualizarParteDaConta(@PathVariable Long id, @RequestBody ContaFinanceiraPatchDTO contaPatchDTO){
        patchValidator.validate(contaPatchDTO);
        return ResponseEntity.status(HttpStatus.OK).body(contaService.atualizarContaParcial(id, contaPatchDTO));
    }

    @Operation(summary = "Apagar conta")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirConta(@PathVariable Long id){
        contaService.excluirConta(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}