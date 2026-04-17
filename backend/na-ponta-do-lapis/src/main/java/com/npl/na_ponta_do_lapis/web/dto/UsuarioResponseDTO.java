package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Usuario;

public record UsuarioResponseDTO(Long id, String nome, String username, String fotoPerfil, String papel) {

    public UsuarioResponseDTO(Usuario usuario){
        this(usuario.getId(), usuario.getNome(), usuario.getUsername(), usuario.getFotoPerfil(), usuario.getPapel().toString());
    }
}
