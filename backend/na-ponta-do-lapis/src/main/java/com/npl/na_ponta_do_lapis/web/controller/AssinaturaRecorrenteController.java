package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.enums.StatusRecorrencia;
import com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter;
import com.npl.na_ponta_do_lapis.service.AssinaturaRecorrenteService;
import com.npl.na_ponta_do_lapis.service.security.ApiRateLimitService;
import com.npl.na_ponta_do_lapis.web.dto.AssinaturaRecorrenteRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.AssinaturaRecorrenteResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.AssinaturasResumoDTO;
import com.npl.na_ponta_do_lapis.web.dto.ParcelamentoResumoDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacoesResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/assinaturas")
@Tag(name = "Assinaturas", description = "Gerenciamento de compras recorrentes e assinaturas")
public class AssinaturaRecorrenteController {

    private final AssinaturaRecorrenteService assinaturaService;
    private final ApiRateLimitService apiRateLimitService;

    public AssinaturaRecorrenteController(
            AssinaturaRecorrenteService assinaturaService,
            ApiRateLimitService apiRateLimitService) {
        this.assinaturaService = assinaturaService;
        this.apiRateLimitService = apiRateLimitService;
    }

    @Operation(summary = "Listar assinaturas e resumo de gastos fixos")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping
    public ResponseEntity<AssinaturasResumoDTO> listar() {
        return ResponseEntity.ok(assinaturaService.listarMinhas());
    }

    @Operation(summary = "Listar parcelamentos em aberto do usuário")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/parcelamentos")
    public ResponseEntity<List<ParcelamentoResumoDTO>> listarParcelamentosEmAberto() {
        return ResponseEntity.ok(assinaturaService.listarParcelamentosEmAberto());
    }

    @Operation(summary = "Criar nova assinatura/compra recorrente")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PostMapping
    public ResponseEntity<AssinaturaRecorrenteResponseDTO> criar(
            @Valid @RequestBody AssinaturaRecorrenteRequestDTO dto,
            HttpServletRequest request) throws AccessDeniedException {
        apiRateLimitService.enforce(
                "assinaturas:criar",
                JwtAuthFilter.getEmailUsuarioLogado(),
                apiRateLimitService.resolveClientIp(request),
                20,
                Duration.ofMinutes(1));
        return ResponseEntity.status(HttpStatus.CREATED).body(assinaturaService.criar(dto));
    }

    @Operation(summary = "Atualizar assinatura existente")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PutMapping("/{id}")
    public ResponseEntity<AssinaturaRecorrenteResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AssinaturaRecorrenteRequestDTO dto) throws AccessDeniedException {
        return ResponseEntity.ok(assinaturaService.atualizar(id, dto));
    }

    @Operation(summary = "Alterar status da assinatura (ATIVA, PAUSADA, CANCELADA)")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AssinaturaRecorrenteResponseDTO> alterarStatus(
            @PathVariable Long id,
            @RequestParam StatusRecorrencia status) throws AccessDeniedException {
        return ResponseEntity.ok(assinaturaService.alterarStatus(id, status));
    }

    @Operation(summary = "Gerar manualmente a transação do ciclo atual")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PostMapping("/{id}/lancar")
    public ResponseEntity<TransacoesResponseDTO> lancarAgora(
            @PathVariable Long id,
            HttpServletRequest request) throws AccessDeniedException {
        apiRateLimitService.enforce(
                "assinaturas:lancar",
                JwtAuthFilter.getEmailUsuarioLogado(),
                apiRateLimitService.resolveClientIp(request),
                10,
                Duration.ofMinutes(1));
        return ResponseEntity.status(HttpStatus.CREATED).body(assinaturaService.lancarCicloManualmente(id));
    }

    @Operation(summary = "Excluir assinatura")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) throws AccessDeniedException {
        assinaturaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
