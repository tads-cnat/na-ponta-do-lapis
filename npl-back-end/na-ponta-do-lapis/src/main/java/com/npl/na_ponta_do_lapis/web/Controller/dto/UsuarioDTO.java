package com.npl.na_ponta_do_lapis.web.Controller.dto;

import com.npl.na_ponta_do_lapis.entity.Enums.Papel;
import com.npl.na_ponta_do_lapis.entity.Usuario;

public record UsuarioDTO(String nome, String email, String username, String senha) {

    public Usuario toEntity(){
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setUsername(username);
        usuario.setFotoPerfil(null);
        usuario.setPapel(Papel.USUARIO);
        return usuario;
    }
}
