package com.npl.na_ponta_do_lapis.service.admin;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    private final PasswordEncoder passwordEncoder;
    private UsuarioRepository usuarioRepository;

    public AdminServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void criarContaAdmin(){
        Optional<Usuario> admin = usuarioRepository.findByEmail("admin@test.com");
        if (admin.isEmpty()){
            Usuario usuario = new Usuario();
            usuario.setNome("Admin_Site");
            usuario.setPapel(Papel.ADMIN_SITE);
            usuario.setUsername("admin");
            usuario.setEmail("admin@test.com");
            usuario.setFotoPerfil(null);
            usuario.setFamilia(null);
            usuario.setSenha(passwordEncoder.encode("admin123"));
            usuarioRepository.save(usuario);
            System.out.println("Conta de Admin criada com sucesso!");
        } else {
            System.out.println("Já existe um Admin!");
        }
    }


    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findByPapelIn(List.of(Papel.ADMIN_SITE));
    }
}
