package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.service.ChaveIaService;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/integracoes/ia/chaves")
@Tag(name = "Chaves de IA", description = "Gerenciamento seguro de chaves de API de IA do usuário")
public class ChaveIaController {

    private final ChaveIaService chaveIaService;

    public ChaveIaController(ChaveIaService chaveIaService) {
        this.chaveIaService = chaveIaService;
    }

    @Operation(summary = "Listar chaves de IA cadastradas do usuário logado")
    @GetMapping
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<List<ChaveIaResponseDTO>> listarChaves(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(chaveIaService.listarChaves(usuario));
    }

    @Operation(summary = "Cadastrar nova chave de IA criptografada")
    @PostMapping
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<ChaveIaResponseDTO> cadastrarChave(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody ChaveIaRequestDTO dto) {
        ChaveIaResponseDTO response = chaveIaService.cadastrarChave(usuario, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Definir uma chave de IA como padrão")
    @PatchMapping("/{id}/padrao")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<Void> definirComoPadrao(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long id) {
        chaveIaService.definirComoPadrao(usuario, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Excluir uma chave de IA")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<Void> deletarChave(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long id) {
        chaveIaService.deletarChave(usuario, id);
        return ResponseEntity.noContent().build();
    }
}

