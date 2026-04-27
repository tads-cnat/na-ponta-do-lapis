package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import com.npl.na_ponta_do_lapis.service.ConviteService;
import com.npl.na_ponta_do_lapis.service.UsuarioService;
import com.npl.na_ponta_do_lapis.web.dto.ConviteDTO;
import com.npl.na_ponta_do_lapis.web.dto.ConviteResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/convites")
@Tag(name = "Convite", description = "Gerenciamento de Convites para Famílias")
public class ConviteController {

    private final ConviteService conviteService;
    private final UsuarioService usuarioService;

    public ConviteController(ConviteService conviteService, UsuarioService usuarioService) {
        this.conviteService = conviteService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar convites pendentes para o usuário autenticado")
    @GetMapping("/me")
    public ResponseEntity<List<ConviteResponseDTO>> listarPendentes(
            Principal principal,
            @RequestParam(required = false) String username
    ) {
        Usuario usuarioAutenticado = usuarioService.buscarUsuarioAutenticado(principal, username);
        return ResponseEntity.ok(conviteService.listarPendentes(usuarioAutenticado));
    }

    @Operation(summary = "Enviar convite para usuário")
    @PostMapping
    public ResponseEntity<ConviteResponseDTO> enviarConvite(
            @RequestBody ConviteDTO conviteDTO,
            Principal principal,
            @RequestParam(required = false) String username
    ) {
        Usuario solicitante = usuarioService.buscarUsuarioAutenticado(principal, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(conviteService.enviarConvite(conviteDTO, solicitante));
    }

    @Operation(summary = "Atualizar status do convite")
    @PatchMapping("/{conviteId}")
    public ResponseEntity<ConviteResponseDTO> atualizarStatusConvite(
            @PathVariable Long conviteId,
            @RequestParam StatusConvite status,
            Principal principal,
            @RequestParam(required = false) String username
    ) {
        Usuario usuarioAutenticado = usuarioService.buscarUsuarioAutenticado(principal, username);

        if (status == StatusConvite.ACEITO) {
            return ResponseEntity.ok(conviteService.aceitarConvite(conviteId, usuarioAutenticado));
        }

        if (status == StatusConvite.RECUSADO) {
            return ResponseEntity.ok(conviteService.recusarConvite(conviteId, usuarioAutenticado));
        }

        throw new ResponseStatusException(BAD_REQUEST, "Status inválido para atualização do convite");
    }
}
