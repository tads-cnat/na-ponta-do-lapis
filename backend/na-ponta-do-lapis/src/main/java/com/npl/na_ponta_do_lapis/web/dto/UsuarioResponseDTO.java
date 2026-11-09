package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    String username,
    String fotoPerfil,
    String papel,
    Long idFamilia,
    Moeda moedaPadrao
) {

    public UsuarioResponseDTO(Usuario usuario){
        this(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getUsername(),
            usuario.getFotoPerfil(),
            usuario.getPapel().toString(),
            usuario.getFamilia() != null ? usuario.getFamilia().getId() : null,
            usuario.getMoedaPadrao()
        );
    }
}
