package com.npl.na_ponta_do_lapis.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO (
    String nome,
    @Email(message = "O email é obrigatório")
    String email,

    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres ")
    String senha
){}
