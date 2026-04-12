package com.npl.na_ponta_do_lapis.web.Controller;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.service.UsuarioService;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
@Tag(name = "Usuarios", description = "Gerenciamento de Usuarios")
public class UsuarioController {

    private UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar Usuários")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listUsuarios(){
        List<Usuario> usuario = usuarioService.listarUsuarios();
        List<UsuarioResponseDTO> response = usuario.stream()
                .map(u -> new UsuarioResponseDTO(u))
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Buscar por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id){
        Usuario usuario = usuarioService.buscarUsuarioPorId(id);
        UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO(usuario);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponseDTO);
    }

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

    @Operation(summary = "Deletar Usuário")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id){
        usuarioService.excluirUsuario(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
