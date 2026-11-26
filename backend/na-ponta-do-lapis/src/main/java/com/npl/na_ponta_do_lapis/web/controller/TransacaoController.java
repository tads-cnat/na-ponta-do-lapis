package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter;
import com.npl.na_ponta_do_lapis.service.TransacaoService;
import com.npl.na_ponta_do_lapis.service.security.ApiRateLimitService;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacoesExclusaoLoteDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacoesResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.Map;

@RestController
@RequestMapping("/transacoes")
@Tag(name = "Transacoes", description = "Gerenciamento de Transacoes")
public class TransacaoController {

    private static final int MAX_TRANSACOES_LOTE = 300;

    private final TransacaoService transacaoService;
    private final ApiRateLimitService apiRateLimitService;

    public TransacaoController(TransacaoService transacaoService, ApiRateLimitService apiRateLimitService) {
        this.transacaoService = transacaoService;
        this.apiRateLimitService = apiRateLimitService;
    }

    @Operation(summary = "Criar Transacao (simples ou parcelada)",
            description = "Com parcelado=true, 'valor' é o total e são geradas 'totalParcelas' transações.")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PostMapping
    public ResponseEntity<List<TransacoesResponseDTO>> criarTransacao(
            @Valid @RequestBody TransacaoRequestDTO transacao,
            HttpServletRequest request) throws AccessDeniedException {
        apiRateLimitService.enforce(
                "transacoes:criar",
                JwtAuthFilter.getEmailUsuarioLogado(),
                apiRateLimitService.resolveClientIp(request),
                30,
                Duration.ofMinutes(1));
        List<Transacao> criadas = transacaoService.criarTransacao(transacao);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(criadas.stream().map(TransacoesResponseDTO::new).toList());
    }

    @Operation(summary = "Listar Transacoe do Usuário Logado")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/me")
    public ResponseEntity<List<TransacoesResponseDTO>> listarTransacoesUsuarioLogado() {
        List<Transacao> transacoes = transacaoService.listarTransacoesUsuarioNaSessao();
        List<TransacoesResponseDTO> response = transacoes.stream()
                .map(t -> new TransacoesResponseDTO(t))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar Transacao por Descrição")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/buscar")
    public ResponseEntity<List<TransacoesResponseDTO>> buscarPorDescricao(@RequestParam String descricao) {
        List<Transacao> transacao = transacaoService.buscarPorDescricao(descricao);

        List<TransacoesResponseDTO> response = transacao.stream()
                .map(t -> new TransacoesResponseDTO(t))
                .toList();
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Buscar Transacao por ID")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/{id}")
    public ResponseEntity<TransacoesResponseDTO> buscarPorId(@PathVariable Long id) {
        Transacao transacao = transacaoService.buscarPorId(id);

        return ResponseEntity.ok(new TransacoesResponseDTO(transacao));
    }

    @Operation(summary = "Atualizar Transacao")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PutMapping("/{id}")
    public ResponseEntity<TransacoesResponseDTO> atualizarTransacao(
           @PathVariable Long id,
           @Valid  @RequestBody TransacaoRequestDTO transacao) throws AccessDeniedException {
        Transacao atualizada = transacaoService.atualizarTransacao(id, transacao);
        return ResponseEntity.ok(new TransacoesResponseDTO(atualizada));
    }

    @Operation(summary = "Deletar Transacao")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws AccessDeniedException {
        transacaoService.removerTransacao(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Deletar Transações em Lote")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PostMapping("/excluir-lote")
    public ResponseEntity<Void> removerEmLote(
            @Valid @RequestBody TransacoesExclusaoLoteDTO dto,
            HttpServletRequest request)
            throws AccessDeniedException {
        apiRateLimitService.enforce(
                "transacoes:excluir-lote",
                JwtAuthFilter.getEmailUsuarioLogado(),
                apiRateLimitService.resolveClientIp(request),
                10,
                Duration.ofMinutes(1));
        transacaoService.removerTransacoesEmLote(dto.ids());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Confirmar pagamento de uma transação pendente")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PatchMapping("/{id}/confirmar-pagamento")
    public ResponseEntity<TransacoesResponseDTO> confirmarPagamento(@PathVariable Long id)
            throws AccessDeniedException {
        Transacao confirmada = transacaoService.confirmarPagamento(id);
        return ResponseEntity.ok(new TransacoesResponseDTO(confirmada));
    }

    @Operation(summary = "Salvar Transações em Lote")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transações salvas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou conta não encontrada", content = @Content(schema = @Schema(example = "{\"error\": \"Conta não encontrada\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno ao salvar transações")
    })
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PostMapping("/lote")
    public ResponseEntity<?> salvarTransacoesEmLote(
            @RequestBody List<TransacaoFaturaDTO> transacoes,
            @RequestParam("contaId") Long contaId,
            HttpServletRequest request) {

        apiRateLimitService.enforce(
                "transacoes:lote",
                JwtAuthFilter.getEmailUsuarioLogado(),
                apiRateLimitService.resolveClientIp(request),
                5,
                Duration.ofMinutes(1));

        if (transacoes == null || transacoes.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lista de transações vazia."));
        }

        if (transacoes.size() > MAX_TRANSACOES_LOTE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Limite máximo de " + MAX_TRANSACOES_LOTE + " transações por lote excedido."));
        }
        try {
            List<Transacao> salvas = transacaoService.salvarFaturaEmLote(transacoes, contaId);
            List<TransacoesResponseDTO> response = salvas.stream()
                    .map(TransacoesResponseDTO::new)
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Listar parcelas de um grupo")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/parcelamento/{grupoId}")
    public ResponseEntity<List<TransacoesResponseDTO>> listarParcelamento(
            @PathVariable String grupoId) throws AccessDeniedException {
        List<Transacao> parcelas = transacaoService.listarParcelamento(grupoId);
        return ResponseEntity.ok(parcelas.stream().map(TransacoesResponseDTO::new).toList());
    }

    @Operation(summary = "Gerar as parcelas futuras de um grupo")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PostMapping("/parcelamento/{grupoId}/futuras")
    public ResponseEntity<List<TransacoesResponseDTO>> gerarParcelasFuturas(
            @PathVariable String grupoId,
            HttpServletRequest request) throws AccessDeniedException {
        apiRateLimitService.enforce(
                "transacoes:parcelas",
                JwtAuthFilter.getEmailUsuarioLogado(),
                apiRateLimitService.resolveClientIp(request),
                10,
                Duration.ofMinutes(1));
        List<Transacao> parcelas = transacaoService.gerarParcelasFuturas(grupoId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parcelas.stream().map(TransacoesResponseDTO::new).toList());
    }

    @Operation(summary = "Gerar as parcelas anteriores de um grupo para histórico")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PostMapping("/parcelamento/{grupoId}/anteriores")
    public ResponseEntity<List<TransacoesResponseDTO>> gerarParcelasAnteriores(
            @PathVariable String grupoId,
            HttpServletRequest request) throws AccessDeniedException {
        apiRateLimitService.enforce(
                "transacoes:parcelas",
                JwtAuthFilter.getEmailUsuarioLogado(),
                apiRateLimitService.resolveClientIp(request),
                10,
                Duration.ofMinutes(1));
        List<Transacao> parcelas = transacaoService.gerarParcelasAnteriores(grupoId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parcelas.stream().map(TransacoesResponseDTO::new).toList());
    }

    @Operation(summary = "Excluir um grupo de parcelamento")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @DeleteMapping("/parcelamento/{grupoId}")
    public ResponseEntity<Void> excluirParcelamento(
            @PathVariable String grupoId,
            @RequestParam(value = "apenasFuturas", defaultValue = "false") boolean apenasFuturas)
            throws AccessDeniedException {
        transacaoService.excluirParcelamento(grupoId, apenasFuturas);
        return ResponseEntity.noContent().build();
    }
}
