package com.npl.na_ponta_do_lapis.web.dto.auth;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank
        @NotNull
        @Size(min = 3)
        String nomeCompleto ,
        @Email
        @NotBlank
        @NotNull
        String email,
        @NotBlank
        @NotNull
        @Size(min = 5)
        String username,
        String password,
        String passwordConfirmation
) {
    public Usuario toEntity(){
        Usuario usuario = new Usuario();
        usuario.setNome(nomeCompleto);
        usuario.setEmail(email);
        usuario.setUsername(username);
        usuario.setFotoPerfil(null);
        usuario.setPapel(Papel.USUARIO);
        return usuario;
    }
}
