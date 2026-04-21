package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.service.TransacaoService;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacoesResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
@Tag(name = "Transacoes", description = "Gerenciamento de Transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Operation(summary = "Criar Transacao")
    @PostMapping
    public ResponseEntity<TransacoesResponseDTO> criarTransacao(@Valid @RequestBody TransacaoRequestDTO transacao) {
        Transacao novaTransacao = transacaoService.criarTransacao(transacao);
        TransacoesResponseDTO responseDTO = new TransacoesResponseDTO(novaTransacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Listar Transacoes")
    @GetMapping
    public ResponseEntity<List<TransacoesResponseDTO>> listarTransacoes() {
        List<Transacao> transacoes = transacaoService.listarTransacoes();
        List<TransacoesResponseDTO> response = transacoes.stream()
                .map(t -> new TransacoesResponseDTO(t))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar Transacao por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TransacoesResponseDTO> buscarPorId(@PathVariable Long id) {
        Transacao transacao = transacaoService.buscarPorId(id);

        return ResponseEntity.ok(new TransacoesResponseDTO(transacao));
    }

    @Operation(summary = "Atualizar Transacao")
    @PutMapping("/{id}")
    public ResponseEntity<TransacoesResponseDTO> atualizarTransacao(
           @PathVariable Long id,
           @Valid  @RequestBody TransacaoRequestDTO transacao) {
        Transacao atualizada = transacaoService.atualizarTransacao(id, transacao);
        return ResponseEntity.ok(new TransacoesResponseDTO(atualizada));
    }

    @Operation(summary = "Deletar Transacao")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        transacaoService.removerTransacao(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
