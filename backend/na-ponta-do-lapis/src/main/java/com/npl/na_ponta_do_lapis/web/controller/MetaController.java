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

import com.npl.na_ponta_do_lapis.service.MetaService;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/metas")
@Tag(name = "Metas", description = "Gerenciamento de metas financeiras")
public class MetaController {
    private final MetaService metaService;

    public MetaController(MetaService service) {
        this.metaService = service;
    }

    @Operation(summary = "Listar metas")
    @GetMapping
    public ResponseEntity<List<MetaResponseDTO>> listar(){
        return ResponseEntity.ok(metaService.listarTodas());
    }

    @Operation(summary = "Buscar por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meta encontrada"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<MetaResponseDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(metaService.buscarPorId(id));
    }

    @Operation(summary = "Criar meta")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Meta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação")
    })    
    @PostMapping
    public ResponseEntity<MetaResponseDTO> criar(@RequestBody @Valid MetaDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.criar(dto));
    }
    
    @Operation(summary = "Atualizar meta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MetaResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid MetaDTO dto){
        return ResponseEntity.ok(metaService.atualizar(id, dto));
    }

    @Operation(summary = "Deletar meta")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Meta deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
        metaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
