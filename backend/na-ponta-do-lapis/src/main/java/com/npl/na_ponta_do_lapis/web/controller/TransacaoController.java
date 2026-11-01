package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.service.TransacaoService;
import com.npl.na_ponta_do_lapis.web.dto.CompraParceladaRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacoesResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transacoes")
@Tag(name = "Transacoes", description = "Gerenciamento de Transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Operation(summary = "Criar Transacao")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PostMapping
    public ResponseEntity<TransacoesResponseDTO> criarTransacao(@Valid @RequestBody TransacaoRequestDTO transacao) throws AccessDeniedException {
        Transacao novaTransacao = transacaoService.criarTransacao(transacao);
        TransacoesResponseDTO responseDTO = new TransacoesResponseDTO(novaTransacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Listar Transacoe do Usuário Logado")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @GetMapping("/me")
    public ResponseEntity<List<TransacoesResponseDTO>> listarTransacoesUsuarioLogado() {
        List<Transacao> transacoes = transacaoService.listarTransacoesUsuarioNaSessao();
        List<TransacoesResponseDTO> response = transacoes.stream()
                .map(t -> new TransacoesResponseDTO(t))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar Transacao por Descrição")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @GetMapping("/buscar")
    public ResponseEntity<List<TransacoesResponseDTO>> buscarPorDescricao(@RequestParam String descricao) {
        List<Transacao> transacao = transacaoService.buscarPorDescricao(descricao);

        List<TransacoesResponseDTO> response = transacao.stream()
                .map(t -> new TransacoesResponseDTO(t))
                .toList();
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Buscar Transacao por ID")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @GetMapping("/{id}")
    public ResponseEntity<TransacoesResponseDTO> buscarPorId(@PathVariable Long id) {
        Transacao transacao = transacaoService.buscarPorId(id);

        return ResponseEntity.ok(new TransacoesResponseDTO(transacao));
    }

    @Operation(summary = "Atualizar Transacao")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PutMapping("/{id}")
    public ResponseEntity<TransacoesResponseDTO> atualizarTransacao(
           @PathVariable Long id,
           @Valid  @RequestBody TransacaoRequestDTO transacao) throws AccessDeniedException {
        Transacao atualizada = transacaoService.atualizarTransacao(id, transacao);
        return ResponseEntity.ok(new TransacoesResponseDTO(atualizada));
    }

    @Operation(summary = "Deletar Transacao")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws AccessDeniedException {
        transacaoService.removerTransacao(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Salvar Transações em Lote")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transações salvas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou conta não encontrada", content = @Content(schema = @Schema(example = "{\"error\": \"Conta não encontrada\"}"))),
        @ApiResponse(responseCode = "500", description = "Erro interno ao salvar transações")
    })
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PostMapping("/lote")
    public ResponseEntity<?> salvarTransacoesEmLote(
            @RequestBody List<TransacaoFaturaDTO> transacoes,
            @RequestParam("contaId") Long contaId) {

        if (transacoes == null || transacoes.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lista de transações vazia."));
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

    @Operation(summary = "Criar compra parcelada")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Parcelas criadas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Sem permissão na conta ou marcador")
    })
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PostMapping("/parcelada")
    public ResponseEntity<List<TransacoesResponseDTO>> criarCompraParcelada(
            @Valid @RequestBody CompraParceladaRequestDTO dto) throws AccessDeniedException {
        List<Transacao> parcelas = transacaoService.criarCompraParcelada(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parcelas.stream().map(TransacoesResponseDTO::new).toList());
    }

    @Operation(summary = "Listar parcelas de um grupo")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @GetMapping("/parcelamento/{grupoId}")
    public ResponseEntity<List<TransacoesResponseDTO>> listarParcelamento(
            @PathVariable String grupoId) throws AccessDeniedException {
        List<Transacao> parcelas = transacaoService.listarParcelamento(grupoId);
        return ResponseEntity.ok(parcelas.stream().map(TransacoesResponseDTO::new).toList());
    }

    @Operation(summary = "Gerar as parcelas futuras de um grupo")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PostMapping("/parcelamento/{grupoId}/futuras")
    public ResponseEntity<List<TransacoesResponseDTO>> gerarParcelasFuturas(
            @PathVariable String grupoId) throws AccessDeniedException {
        List<Transacao> parcelas = transacaoService.gerarParcelasFuturas(grupoId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parcelas.stream().map(TransacoesResponseDTO::new).toList());
    }

    @Operation(summary = "Excluir um grupo de parcelamento")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @DeleteMapping("/parcelamento/{grupoId}")
    public ResponseEntity<Void> excluirParcelamento(
            @PathVariable String grupoId,
            @RequestParam(value = "apenasFuturas", defaultValue = "false") boolean apenasFuturas)
            throws AccessDeniedException {
        transacaoService.excluirParcelamento(grupoId, apenasFuturas);
        return ResponseEntity.noContent().build();
    }
}
