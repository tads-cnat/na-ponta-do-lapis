package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.service.openfinance.OpenFinanceSyncService;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceAdicionarItemRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceConectarRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceItemDTO;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceStatusResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/integracoes/open-finance")
@Tag(name = "Open Finance", description = "Integração e sincronização bancária via Open Finance (Meu Pluggy)")
public class OpenFinanceController {

    private final OpenFinanceSyncService syncService;

    public OpenFinanceController(OpenFinanceSyncService syncService) {
        this.syncService = syncService;
    }

    @Operation(summary = "Obter status da integração de Open Finance")
    @GetMapping("/status")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<OpenFinanceStatusResponseDTO> obterStatus(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(syncService.obterStatus(usuario));
    }

    @Operation(summary = "Conectar credenciais do Meu Pluggy / Open Finance")
    @PostMapping("/conectar")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<OpenFinanceStatusResponseDTO> conectar(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody OpenFinanceConectarRequestDTO dto) {
        return ResponseEntity.ok(syncService.conectar(usuario, dto));
    }

    @Operation(summary = "Sincronizar transações e saldo manualmente (limite de 5x ao dia)")
    @PostMapping("/sincronizar")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<OpenFinanceStatusResponseDTO> sincronizar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(syncService.sincronizarManual(usuario));
    }

    @Operation(summary = "Gerar Connect Token para abrir o widget oficial do Pluggy Connect")
    @PostMapping("/connect-token")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<java.util.Map<String, String>> gerarConnectToken(@AuthenticationPrincipal Usuario usuario) {
        String token = syncService.gerarConnectToken(usuario);
        return ResponseEntity.ok(java.util.Map.of("connectToken", token));
    }

    @Operation(summary = "Listar conexões bancárias (itens) cadastradas do usuário")
    @GetMapping("/items")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<List<OpenFinanceItemDTO>> listarItens(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(syncService.listarItensConectados(usuario));
    }

    @Operation(summary = "Vincular e sincronizar conexão bancária por Item ID")
    @PostMapping("/items")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<OpenFinanceStatusResponseDTO> adicionarItem(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody OpenFinanceAdicionarItemRequestDTO dto) {
        return ResponseEntity.ok(syncService.adicionarItem(usuario, dto.itemId()));
    }

    @Operation(summary = "Remover conexão bancária por Item ID")
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<Void> removerItem(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable String itemId) {
        syncService.removerItem(usuario, itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desconectar integração de Open Finance")
    @DeleteMapping("/desconectar")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<Void> desconectar(@AuthenticationPrincipal Usuario usuario) {
        syncService.desconectar(usuario);
        return ResponseEntity.noContent().build();
    }
}

