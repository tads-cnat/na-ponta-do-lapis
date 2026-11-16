package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO (
    String nome,
    @Email(message = "O email deve ser válido")
    String email,

    String senhaAtual,

    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres ")
    String senha,

    Moeda moedaPadrao,

    @Size(max = 200, message = "A pergunta de segurança deve ter no máximo 200 caracteres")
    String perguntaSeguranca,

    @Size(max = 200, message = "A resposta de segurança deve ter no máximo 200 caracteres")
    String respostaSeguranca
) {
    public UsuarioUpdateDTO(String nome, String email, String senha) {
        this(nome, email, null, senha, null, null, null);
    }
}
