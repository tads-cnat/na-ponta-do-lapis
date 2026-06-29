package com.npl.na_ponta_do_lapis.web.controller;


import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.service.FamiliaService;
import com.npl.na_ponta_do_lapis.service.UsuarioService;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFamiliaDTO;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraFamiliaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/familias")
@Tag(name = "Família", description = "Gerenciamento de Famílias")
public class FamiliaController {

    private FamiliaService familiaService;
    private UsuarioService usuarioService;

    @Autowired
    public FamiliaController(FamiliaService familiaS, UsuarioService usuarioService) {
        this.familiaService = familiaS;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar Famílias")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping
    public ResponseEntity<List<FamiliaResponseDTO>> listarFamilias() {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarFamilias());
    }

    @Operation(summary = "Lista de Transações da Família")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/{familiaId}/transacoes")
    public ResponseEntity<List<TransacaoFamiliaDTO>> listarTransacoesDaFamilia(@PathVariable Long familiaId) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarTransacoesDaFamilia(familiaId));
    }

    @Operation(summary = "Lista de Contas da Família")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/{familiaId}/contas")
    public ResponseEntity<List<ContaFinanceiraFamiliaDTO>> listarContasDaFamilia(@PathVariable Long familiaId) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarContasDaFamilia(familiaId));
    }

    @Operation(summary = "Lista de Transações da Família por Descrição")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/{familiaId}/transacoes/descricao")
    public ResponseEntity<List<TransacaoFamiliaDTO>> listarTransacoesDaFamiliaPorDescricao(@PathVariable Long familiaId, @RequestParam String descricao) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarTransacoesDaFamiliaPorDescricao(familiaId, descricao));
    }

    @Operation(summary = "Buscar por Id")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/{familiaId}")
    public ResponseEntity<FamiliaResponseDTO> buscarPorId(@PathVariable Long familiaId) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.buscarFamiliaPorID(familiaId));
    }

    @Operation(summary = "Cadastrar Família")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PostMapping
    public ResponseEntity<FamiliaResponseDTO> cadastrarFamilia(@RequestBody FamiliaDTO familiaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(familiaService.criarFamilia(familiaDTO));
    }

    @Operation(summary = "Excluir Família")
    @PreAuthorize("hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @DeleteMapping("/{familiaId}")
    public ResponseEntity<Void> excluirFamilia(@PathVariable Long familiaId) {
        familiaService.excluirFamilia(familiaId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Editar parte da Família")
    @PreAuthorize("hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PatchMapping("/{familiaId}")
    public ResponseEntity<FamiliaResponseDTO> editarParteFamilia(@PathVariable Long familiaId,
                                                             @RequestParam(required = false) String nome,
                                                             @RequestParam(required = false) String foto) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.editarFamilia(familiaId, new FamiliaDTO(nome, foto)));
    }

    @Operation(summary = "Editar tudo da Família")
    @PreAuthorize("hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PutMapping("/{familiaId}")
    public ResponseEntity<FamiliaResponseDTO> editarTudoFamilia(@PathVariable Long familiaId, @RequestBody FamiliaDTO familiaDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.editarFamilia(familiaId, familiaDTO));
    }

    @Operation(summary = "Listar membros da família")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @GetMapping("/{familiaId}/membros")
    public ResponseEntity<List<UsuarioResponseDTO>> listarMembrosDaFamilia(@PathVariable Long familiaId) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarMembrosDaFamilia(familiaId));
    }

    @Operation(summary = "Adicionar usuário na família")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PostMapping("/{familiaId}/membros")
    public ResponseEntity<FamiliaResponseDTO> adicionarUsuarioNaFamilia(@PathVariable Long familiaId, @RequestParam String username) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.adicionarUsuarioNaFamilia(username, familiaId));
    }

    @Operation(summary = "Remover usuário da família")
    @PreAuthorize("hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @DeleteMapping("/{familiaId}/membros")
    public ResponseEntity<FamiliaResponseDTO> removerUsuarioDaFamilia(@PathVariable Long familiaId, @RequestParam String username) {
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.removerUsuarioNaFamilia(username, familiaId));
    }

    @Operation(summary = "Promover membro a administrador da família")
    @PreAuthorize("hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    @PatchMapping("/{familiaId}/membros/{userId}")
    public ResponseEntity<UsuarioResponseDTO> promoverAdministrador(
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
