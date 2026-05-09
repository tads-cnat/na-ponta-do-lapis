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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/marcadores")
@Tag(name = "Marcador", description = "Gerenciamento de Marcadores")
public class MarcadorController {

    private final MarcadorService marcadorService;

    public MarcadorController(MarcadorService marcadorService) {
        this.marcadorService = marcadorService;
    }

    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @Operation(summary = "Criar marcador do usuário Logado")
    @PostMapping
    public ResponseEntity<MarcadorResponseDTO> criarMarcador(@Valid @RequestBody MarcadorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(marcadorService.criarMarcador(dto));
    }

    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @Operation(summary = "Listar marcadores do usuário Logado")
    @GetMapping("/me")
    public ResponseEntity<List<MarcadorResponseDTO>> listarMarcadores() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(marcadorService.listarMarcadores());
    }

    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @Operation(summary = "Editar marcador do usuário Logado")
    @PutMapping("/{id}")
    public ResponseEntity<MarcadorResponseDTO> editarMarcador(
            @Valid @RequestBody MarcadorDTO dto,  @PathVariable long id) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(marcadorService.editarMarcador(id ,dto));
    }

    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @Operation(summary = "Excluir marcador do usuário Logado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMarcador(
            @PathVariable Long id) {
        marcadorService.excluirMarcador(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}