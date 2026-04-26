package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.web.exception.UsuarioIdNaoExisteException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void login(){}

    public void logout(){}

    public List<Usuario> listarUsuarios(){
        return usuarioRepository.findAll();
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(
                        () -> new UsuarioIdNaoExisteException("Usuário de ID " + id + " não existe!")
                );
    }

    @Transactional
    public Usuario cadastrarUsuario(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluirUsuario(Long id){
        if (!usuarioRepository.existsById(id)){
            throw new UsuarioIdNaoExisteException("Usuário de ID " + id + " não existe!");
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public void tornarUsuarioAdminSite(Long id){}

    @Transactional
    public void tornarAdminFamilia(Long id){}
}
