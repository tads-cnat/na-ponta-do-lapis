package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Enums.Papel;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        @Email(message = "O email é obrigatório")
        String email,

        String username,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres ")
        String senha
) {

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
