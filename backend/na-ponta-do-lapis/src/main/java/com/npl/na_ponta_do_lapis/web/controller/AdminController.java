package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.service.admin.AdminPainelService;
import com.npl.na_ponta_do_lapis.web.dto.admin.AdminDTOs.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/admin", "/v1/admin"})
@PreAuthorize("hasRole('ADMIN_SITE')")
@Tag(name = "Administração da Plataforma", description = "Endpoints exclusivos para ADMIN_SITE")
public class AdminController {

    private final AdminPainelService adminPainelService;

    public AdminController(AdminPainelService adminPainelService) {
        this.adminPainelService = adminPainelService;
    }

    @GetMapping("/dashboard/resumo")
    public ResponseEntity<AdminDashboardResumoDTO> resumoDashboard(
            @RequestParam(defaultValue = "30") int dias
    ) {
        return ResponseEntity.ok(adminPainelService.obterResumoDashboard(dias));
    }

    // --- Usuários ---

    @GetMapping("/usuarios")
    public ResponseEntity<AdminPageResponseDTO<AdminUsuarioResumoDTO>> listarUsuarios(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Papel papel,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Boolean emailVerificado,
            @RequestParam(required = false) Boolean onboardingConcluido,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminPainelService.listarUsuarios(q, papel, ativo, emailVerificado, onboardingConcluido, page, size));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<AdminUsuarioDetalheDTO> obterUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(adminPainelService.obterDetalheUsuario(id));
    }

    @PatchMapping("/usuarios/{id}/suspensao")
    public ResponseEntity<AdminUsuarioDetalheDTO> alterarSuspensao(
            @PathVariable Long id,
            @Valid @RequestBody AdminSuspensaoRequestDTO req,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminPainelService.alterarSuspensao(id, req, extrairIp(request)));
    }

    @PatchMapping("/usuarios/{id}/verificar-email")
    public ResponseEntity<AdminUsuarioDetalheDTO> verificarEmail(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminPainelService.forcarVerificacaoEmail(id, extrairIp(request)));
    }

    @PatchMapping("/usuarios/{id}/papel")
    public ResponseEntity<AdminUsuarioDetalheDTO> alterarPapel(
            @PathVariable Long id,
            @Valid @RequestBody AdminAlterarPapelRequestDTO req,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminPainelService.alterarPapel(id, req, extrairIp(request)));
    }

    @PatchMapping("/usuarios/{id}/reset-onboarding")
    public ResponseEntity<AdminUsuarioDetalheDTO> resetarOnboarding(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminPainelService.resetarOnboarding(id, extrairIp(request)));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> excluirUsuario(
            @PathVariable Long id,
            @Valid @RequestBody AdminExcluirUsuarioRequestDTO req,
            HttpServletRequest request
    ) {
        adminPainelService.excluirUsuarioDefinitivamente(id, req, extrairIp(request));
        return ResponseEntity.noContent().build();
    }

    // --- Famílias ---

    @GetMapping("/familias")
    public ResponseEntity<AdminPageResponseDTO<AdminFamiliaResumoDTO>> listarFamilias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminPainelService.listarFamilias(page, size));
    }

    // --- Catálogos Globais ---

    @GetMapping("/categorias")
    public ResponseEntity<List<AdminCategoriaResumoDTO>> listarCategorias() {
        return ResponseEntity.ok(adminPainelService.listarCategorias());
    }

    @PostMapping("/categorias")
    public ResponseEntity<AdminCategoriaResumoDTO> criarCategoria(
            @Valid @RequestBody AdminCategoriaRequestDTO req,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminPainelService.criarCategoria(req, extrairIp(request)));
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<AdminCategoriaResumoDTO> atualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoriaRequestDTO req,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminPainelService.atualizarCategoria(id, req, extrairIp(request)));
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> excluirCategoria(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        adminPainelService.excluirCategoria(id, extrairIp(request));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conquistas")
    public ResponseEntity<List<AdminConquistaResumoDTO>> listarConquistas() {
        return ResponseEntity.ok(adminPainelService.listarConquistas());
    }

    @PostMapping("/conquistas")
    public ResponseEntity<AdminConquistaResumoDTO> criarConquista(
            @Valid @RequestBody AdminConquistaRequestDTO req,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminPainelService.criarConquista(req, extrairIp(request)));
    }

    @PutMapping("/conquistas/{id}")
    public ResponseEntity<AdminConquistaResumoDTO> atualizarConquista(
            @PathVariable String id,
            @Valid @RequestBody AdminConquistaRequestDTO req,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminPainelService.atualizarConquista(id, req, extrairIp(request)));
    }

    // --- Comunicação ---

    @PostMapping("/notificacoes/enviar")
    public ResponseEntity<AdminNotificacaoResultadoDTO> enviarNotificacao(
            @Valid @RequestBody AdminNotificacaoRequestDTO req,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminPainelService.enviarNotificacao(req, extrairIp(request)));
    }

    // --- Auditoria ---

    @GetMapping("/auditoria")
    public ResponseEntity<AdminPageResponseDTO<AdminAuditLogDTO>> listarAuditoria(
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String adminEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(adminPainelService.listarAuditoria(acao, adminEmail, dataInicio, dataFim, page, size));
    }

    private String extrairIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].strip();
        }
        return request.getRemoteAddr();
    }
}
