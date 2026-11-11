package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.service.ConquistaService;
import com.npl.na_ponta_do_lapis.web.dto.PerfilGamificadoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conquistas")
@Tag(name = "Conquistas & Gamificação", description = "Endpoints de conquistas, badges, XP e nível financeiro do usuário")
public class ConquistaController {

    private final ConquistaService conquistaService;

    public ConquistaController(ConquistaService conquistaService) {
        this.conquistaService = conquistaService;
    }

    @Operation(summary = "Obter resumo gamificado do usuário logado (Nível, XP, Badges e progresso)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<PerfilGamificadoDTO> obterPerfilGamificado(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(conquistaService.obterPerfilGamificado(usuario.getId()));
    }
}

