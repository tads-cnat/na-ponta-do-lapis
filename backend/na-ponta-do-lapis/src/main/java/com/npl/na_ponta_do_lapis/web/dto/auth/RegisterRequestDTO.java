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

        String telefone,

        String password,

        String passwordConfirmation,

        @Size(max = 200, message = "A pergunta de segurança deve ter até 200 caracteres.")
        String perguntaSeguranca,

        @Size(max = 200, message = "A resposta de segurança deve ter até 200 caracteres.")
        String respostaSeguranca
) {
    public Usuario toEntity(){
        Usuario usuario = new Usuario();
        usuario.setNome(nomeCompleto);
        usuario.setEmail(email);
        usuario.setUsername(username);
        usuario.setTelefone(telefone);
        usuario.setFotoPerfil(null);
        usuario.setPapel(Papel.USUARIO);
        return usuario;
    }
}
