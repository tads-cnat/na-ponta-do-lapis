package com.npl.na_ponta_do_lapis.web.controller;


import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.service.FamiliaService;
import com.npl.na_ponta_do_lapis.service.UsuarioService;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaDTO;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/familias")
@Tag(name = "Família", description = "Gerenciamento de Famílias")
public class FamiliaController {

    private final FamiliaService familiaService;
    private final UsuarioService usuarioService;

    public FamiliaController(FamiliaService familiaService, UsuarioService usuarioService) {
        this.familiaService = familiaService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar Famílias")
    @GetMapping
    private ResponseEntity<List<FamiliaResponseDTO>> listarFamilias() {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarFamilias());
    }

    @Operation(summary = "Buscar por Id")
    @GetMapping("/{familiaId}")
    private ResponseEntity<FamiliaResponseDTO> buscarPorId(@PathVariable Long familiaId) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.buscarFamiliaPorID(familiaId));
    }

    @Operation(summary = "Cadastrar Família")
    @PostMapping
    private ResponseEntity<FamiliaResponseDTO> cadastrarFamilia(@RequestBody FamiliaDTO familiaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(familiaService.criarFamilia(familiaDTO));
    }

    @Operation(summary = "Excluir Família")
    @DeleteMapping("/{familiaId}")
    private ResponseEntity<Void> excluirFamilia(@PathVariable Long familiaId) {
        familiaService.excluirFamilia(familiaId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Editar parte da Família")
    @PatchMapping("/{familiaId}")
    private ResponseEntity<FamiliaResponseDTO> editarParteFamilia(@PathVariable Long familiaId,
                                                             @RequestParam(required = false) String nome,
                                                             @RequestParam(required = false) String foto) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.editarFamilia(familiaId, new FamiliaDTO(nome, foto)));
    }

    @Operation(summary = "Editar tudo da Família")
    @PutMapping("/{familiaId}")
    private ResponseEntity<FamiliaResponseDTO> editarTudoFamilia(@PathVariable Long familiaId, @RequestBody FamiliaDTO familiaDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.editarFamilia(familiaId, familiaDTO));
    }

    @Operation(summary = "Listar membros da família")
    @GetMapping("/{familiaId}/membros")
    private ResponseEntity<List<UsuarioResponseDTO>> listarMembrosDaFamilia(@PathVariable Long familiaId) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarMembrosDaFamilia(familiaId));
    }

    @Operation(summary = "Adicionar usuário na família")
    @PostMapping("/{familiaId}/membros")
    private ResponseEntity<FamiliaResponseDTO> adicionarUsuarioNaFamilia(@PathVariable Long familiaId, @RequestParam String username) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.adicionarUsuarioNaFamilia(username, familiaId));
    }

    @Operation(summary = "Remover usuário da família")
    @DeleteMapping("/{familiaId}/membros")
    private ResponseEntity<FamiliaResponseDTO> removerUsuarioDaFamilia(@PathVariable Long familiaId, @RequestParam String username) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.removerUsuarioNaFamilia(username, familiaId));
    }

    @Operation(summary = "Promover membro a administrador da família")
    @PatchMapping("/{familiaId}/membros/{userId}")
    private ResponseEntity<UsuarioResponseDTO> promoverAdministrador(
            @PathVariable Long familiaId,
            @PathVariable Long userId,
            Principal principal,
            @RequestParam(required = false) String username
    ) {
        Usuario solicitante = usuarioService.buscarUsuarioAutenticado(principal, username);

        if (solicitante.getFamilia() == null || !familiaId.equals(solicitante.getFamilia().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "familiaId da rota não corresponde à família do solicitante");
        }

        return ResponseEntity.status(HttpStatus.OK).body(familiaService.promoverParaAdminFamilia(userId, solicitante.getId()));
    }
}
