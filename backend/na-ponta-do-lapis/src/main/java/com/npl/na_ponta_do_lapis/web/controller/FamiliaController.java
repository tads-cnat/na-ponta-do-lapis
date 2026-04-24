package com.npl.na_ponta_do_lapis.web.controller;


import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.service.FamiliaService;
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
@RequestMapping("/familia")
@Tag(name = "Família", description = "Gerenciamento de Famílias")
public class FamiliaController {

    private final FamiliaService familiaService;
    private final UsuarioRepository usuarioRepository;

    public FamiliaController(FamiliaService familiaService, UsuarioRepository usuarioRepository){
        this.familiaService = familiaService;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Listar Famílias")
    @GetMapping
    private ResponseEntity<List<FamiliaResponseDTO>> listarFamilias(){
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarFamilias());
    }

    @Operation(summary = "Buscar por Id")
    @GetMapping("/{id}")
    private ResponseEntity<FamiliaResponseDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.buscarFamiliaPorID(id));
    }

    @Operation(summary = "Cadastrar Família")
    @PostMapping
    private ResponseEntity<FamiliaResponseDTO> cadastrarFamilia(@RequestBody FamiliaDTO familiaDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(familiaService.criarFamilia(familiaDTO));
    }

    @Operation(summary = "Excluir Família")
    @DeleteMapping("/{id}")
    private ResponseEntity<Void> excluirFamilia(@PathVariable Long id){
        familiaService.excluirFamilia(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Editar Família")
    @PutMapping("/{id}")
    private ResponseEntity<FamiliaResponseDTO> editarFamilia(@PathVariable Long id,
                                                             @RequestParam(required = false) String nome,
                                                             @RequestParam(required = false) String foto){
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.editarFamilia(id, new FamiliaDTO(nome, foto)));
    }

    @Operation(summary = "Adicionar usuário na família")
    @PostMapping("/{id}/adicionar-usuario")
    private ResponseEntity<FamiliaResponseDTO> adicionarUsuarioNaFamilia(@PathVariable Long id, @RequestParam String username){
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.adicionarUsuarioNaFamilia(username, id));
    }

    @Operation(summary = "Remover usuário da família")
    @PostMapping("/{id}/remover-usuario")
    private ResponseEntity<FamiliaResponseDTO> removerUsuarioDaFamilia(@PathVariable Long id, @RequestParam String username){
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.removerUsuarioNaFamilia(username, id));
    }

    @Operation(summary= "Promover membro a administrador da família")
    @PostMapping("/{userId}/promover-administrador-familia")
    private ResponseEntity<UsuarioResponseDTO> promoverAdministrador(@PathVariable Long userId, Principal principal){
        Usuario solicitante = buscarUsuarioAutenticado(principal);
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.promoverParaAdmin(userId, solicitante));
    }

    private Usuario buscarUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário autenticado não encontrado"));
    }

}
