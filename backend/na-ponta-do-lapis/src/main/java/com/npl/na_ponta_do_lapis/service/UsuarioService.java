package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.web.exception.UsuarioIdNaoExisteException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

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

    public Usuario buscarUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Usuário de email " + email + " não encontrado!")
        );
    }


    @Transactional
    public void tornarAdminFamilia(Long id){}

//    public Usuario buscarUsuarioAutenticado(Principal principal) {
//        return buscarUsuarioAutenticado(principal, null);
//    }

    public Usuario buscarUsuarioAutenticado(Principal principal, String usernameFallback) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            Usuario usuarioAutenticado = usuarioRepository.findByUsername(principal.getName());
            if (usuarioAutenticado == null) {
                throw new RuntimeException("Usuário autenticado não encontrado");
            }
            return usuarioAutenticado;
        }

        // Temporario: permite operar sem autenticacao formal enquanto o fluxo de login nao existe.
        if (usernameFallback == null || usernameFallback.isBlank()) {
            throw new RuntimeException("Usuário não autenticado. Informe o parâmetro 'username' temporariamente.");
        }

        Usuario usuarioFallback = usuarioRepository.findByUsername(usernameFallback);
        if (usuarioFallback == null) {
            throw new RuntimeException("Usuário informado não encontrado");
        }
        return usuarioFallback;
    }
}
