package com.npl.na_ponta_do_lapis.web.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.security.jwt.JwtUtil;
import com.npl.na_ponta_do_lapis.service.UsuarioService;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioUpdateDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuario")
@Tag(name = "Usuarios", description = "Gerenciamento de Usuarios")
public class UsuarioController {

    private UsuarioService usuarioService;
    private JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil){
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Listar Usuários")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_SITE')")
    public ResponseEntity<List<UsuarioResponseDTO>> listUsuarios(){
        List<Usuario> usuario = usuarioService.listarUsuarios();
        List<UsuarioResponseDTO> response = usuario.stream()
                .map(u -> new UsuarioResponseDTO(u))
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Usuário logado")
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN_SITE') OR hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    public ResponseEntity<UsuarioResponseDTO> me(){
        return ResponseEntity.ok(new UsuarioResponseDTO(usuarioService.buscarUsuarioLogado()));
    }

    @PreAuthorize("hasRole('ADMIN_SITE') or (hasRole('USUARIO') and #id == authentication.principal.id)")
    @Operation(summary = "Buscar por ID")
    @GetMapping("/id/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id){
        Usuario usuario = usuarioService.buscarUsuarioPorId(id);
        UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO(usuario);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponseDTO);
    }

    @PreAuthorize("hasRole('ADMIN_SITE') or hasRole('USUARIO') or hasRole('ADMIN_FAMILIA')")
    @Operation(summary = "Buscar por Username")
    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorUsername(@PathVariable String username) {
        Usuario usuario = usuarioService.buscarUsuarioPorUsername(username);
        UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO(usuario);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponseDTO);
    }

    @PreAuthorize("hasRole('ADMIN_SITE')")
    @Operation(summary = "Cadastrar Usuário")
    @PostMapping()
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody @Valid UsuarioDTO usuarioDTO){
        Usuario usuarioNovo = usuarioDTO.toEntity();
//      usuarioNovo.setSenha(passwordEncoder.encode(usuarioNovo.getSenha()));
        usuarioNovo.setSenha(usuarioDTO.senha());
        Usuario salvo = usuarioService.cadastrarUsuario(usuarioNovo);

        UsuarioResponseDTO response = new UsuarioResponseDTO(salvo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar Usuário Logado (Passe apenas os campos que for atualizar)")
    @PreAuthorize("hasRole('ADMIN_SITE') OR hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    @PatchMapping("/me")
    public ResponseEntity<?> atualizarUsuarioLogado(@RequestBody @Valid UsuarioUpdateDTO usuarioDTO) {
        Usuario usuarioAtualizado = usuarioService.atualizarUsuarioLogado(usuarioDTO);

        if (usuarioDTO.email() != null && !usuarioDTO.email().trim().isEmpty()) {
            String novoToken = jwtUtil.generateToken(
                usuarioAtualizado,
                new Date(),
                usuarioAtualizado.getEmail()
            );
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "usuario", new UsuarioResponseDTO(usuarioAtualizado),
                "token", novoToken
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(new UsuarioResponseDTO(usuarioAtualizado));
    }

    @PreAuthorize("hasRole('ADMIN_SITE')")
    @Operation(summary = "Deletar Usuário")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id){
        usuarioService.excluirUsuario(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
