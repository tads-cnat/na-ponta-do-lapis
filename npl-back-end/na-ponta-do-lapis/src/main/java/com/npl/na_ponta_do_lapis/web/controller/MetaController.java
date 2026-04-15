package com.npl.na_ponta_do_lapis.web.controller;


import com.npl.na_ponta_do_lapis.service.MetaService;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metas")
@Tag(name = "Metas", description = "Gerenciamento de metas financeiras")
public class MetaController {
    private final MetaService service;

    public MetaController(MetaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar metas")
    @GetMapping
    public ResponseEntity<List<MetaResponseDTO>> listar(){
        List<MetaResponseDTO> response = service.listarMetas();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Criar metas")
    @PostMapping
    public ResponseEntity<MetaResponseDTO> criar(@RequestBody @Valid MetaDTO metaDTO){
        MetaResponseDTO novaMeta = service.criarMeta(metaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaMeta);
    }
}
