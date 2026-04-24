package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.service.ConviteService;
import com.npl.na_ponta_do_lapis.web.dto.ConviteDTO;
import com.npl.na_ponta_do_lapis.web.dto.ConviteResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/convite")
@Tag(name = "Convite", description = "Gerenciamento de Convites para Famílias")
public class ConviteController {

    private final ConviteService conviteService;
    private final UsuarioRepository usuarioRepository;

    public ConviteController(ConviteService conviteService, UsuarioRepository usuarioRepository) {
        this.conviteService = conviteService;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Listar convites pendentes para o usuário autenticado")
    @GetMapping("/pendentes")
    public ResponseEntity<List<ConviteResponseDTO>> listarPendentes(Principal principal) {
        Usuario usuarioAutenticado = buscarUsuarioAutenticado(principal);
        return ResponseEntity.ok(conviteService.listarPendentes(usuarioAutenticado));
    }

    @Operation(summary = "Enviar convite para usuário")
    @PostMapping("/convidar")
    public ResponseEntity<ConviteResponseDTO> enviarConvite(
            @RequestBody ConviteDTO conviteDTO,
            Principal principal
    ) {
        Usuario solicitante = buscarUsuarioAutenticado(principal);
        return ResponseEntity.ok(conviteService.enviarConvite(conviteDTO, solicitante));
    }

    @Operation(summary = "Aceitar convite para familia")
    @PostMapping("/{conviteId}/aceitar")
    public ResponseEntity<ConviteResponseDTO> aceitarConvite(
            @PathVariable Long conviteId,
            Principal principal
    ) {
        Usuario usuarioAutenticado = buscarUsuarioAutenticado(principal);
        return ResponseEntity.ok(conviteService.aceitarConvite(conviteId, usuarioAutenticado));
    }

    @Operation(summary = "Recusar convite para familia")
    @PostMapping("/{conviteId}/recusar")
    public ResponseEntity<ConviteResponseDTO> recusarConvite(
            @PathVariable Long conviteId,
            Principal principal
    ) {
        Usuario usuarioAutenticado = buscarUsuarioAutenticado(principal);
        return ResponseEntity.ok(conviteService.recusarConvite(conviteId, usuarioAutenticado));
    }

    private Usuario buscarUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário autenticado não encontrado"));
    }
}
