package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.service.MarcadorService;
import com.npl.na_ponta_do_lapis.service.UsuarioService;
import com.npl.na_ponta_do_lapis.web.dto.MarcadorDTO;
import com.npl.na_ponta_do_lapis.web.dto.MarcadorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/marcadores")
@Tag(name = "Marcador", description = "Gerenciamento de Marcadores")
public class MarcadorController {

    private final MarcadorService marcadorService;
    private final UsuarioService usuarioService;

    public MarcadorController(MarcadorService marcadorService, UsuarioService usuarioService) {
        this.marcadorService = marcadorService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Criar marcador")
    @PostMapping
    private ResponseEntity<MarcadorResponseDTO> criarMarcador(
            @Valid @RequestBody MarcadorDTO dto,
            Principal principal) {

        Usuario usuario = usuarioService.buscarUsuarioAutenticado(principal, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(marcadorService.criarMarcador(dto, usuario));
    }

    @Operation(summary = "Listar marcadores do usuário")
    @GetMapping
    private ResponseEntity<List<MarcadorResponseDTO>> listarMarcadores(Principal principal) {
        Usuario usuario = usuarioService.buscarUsuarioAutenticado(principal, null);
        return ResponseEntity.status(HttpStatus.OK)
                .body(marcadorService.listarMarcadores(usuario));
    }

    @Operation(summary = "Editar marcador")
    @PutMapping("/{id}")
    private ResponseEntity<MarcadorResponseDTO> editarMarcador(
            @PathVariable Long id,
            @Valid @RequestBody MarcadorDTO dto,
            Principal principal) {

        Usuario usuario = usuarioService.buscarUsuarioAutenticado(principal, null);
        return ResponseEntity.status(HttpStatus.OK)
                .body(marcadorService.editarMarcador(id, dto, usuario));
    }

    @Operation(summary = "Excluir marcador")
    @DeleteMapping("/{id}")
    private ResponseEntity<Void> excluirMarcador(
            @PathVariable Long id,
            Principal principal) {

        Usuario usuario = usuarioService.buscarUsuarioAutenticado(principal, null);
        marcadorService.excluirMarcador(id, usuario);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}