package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.service.NotificacaoService;
import com.npl.na_ponta_do_lapis.web.dto.NotificacaoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
@Tag(name = "Notificações", description = "Gerenciamento de notificações in-app do usuário")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @Operation(summary = "Listar as últimas notificações do usuário logado")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(notificacaoService.listarUltimasPorUsuario(usuario.getId()));
    }

    @Operation(summary = "Contar quantidade de notificações não lidas")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/nao-lidas/count")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(@AuthenticationPrincipal Usuario usuario) {
        long count = notificacaoService.contarNaoLidasPorUsuario(usuario.getId());
        return ResponseEntity.ok(Map.of("totalNaoLidas", count));
    }

    @Operation(summary = "Marcar uma notificação específica como lida")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/ler")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        notificacaoService.marcarComoLida(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marcar todas as notificações do usuário como lidas")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/ler-todas")
    public ResponseEntity<Void> marcarTodasComoLidas(@AuthenticationPrincipal Usuario usuario) {
        notificacaoService.marcarTodasComoLidas(usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Excluir uma notificação")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        notificacaoService.excluir(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}

