package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.web.Controller.Exception.UsuarioIdNaoExisteException;
import com.npl.na_ponta_do_lapis.web.Controller.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.Controller.dto.UsuarioResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void login(){}

    public void logout(){}

    public List<UsuarioResponseDTO> listarUsuarios(){
        return usuarioRepository.findAll()
                .stream()
                .map(usuario -> new UsuarioResponseDTO(usuario))
                .toList();
    }

    public UsuarioResponseDTO buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> new UsuarioResponseDTO(usuario)).orElseThrow(
                        () -> new UsuarioIdNaoExisteException("Usuário de ID " + id + " não existe!")
                );
    }

    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioDTO usuarioDTO){
        Usuario usuarioNovo = usuarioDTO.toEntity();
//        usuarioNovo.setSenha(passwordEncoder.encode(usuarioNovo.getSenha()));
        usuarioNovo.setSenha(usuarioDTO.senha());

        usuarioRepository.save(usuarioNovo);
        return new UsuarioResponseDTO(usuarioNovo);
    }

    @Transactional
    public void excluirUsuario(Long id){
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public void tornarUsuarioAdminSite(Long id){}

    @Transactional
    public void tornarAdminFamilia(Long id){}
}
