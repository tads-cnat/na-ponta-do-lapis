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
<<<<<<< HEAD
<<<<<<< HEAD
    String senha,

    Moeda moedaPadrao
) {
    public UsuarioUpdateDTO(String nome, String email, String senha) {
        this(nome, email, null, senha, null);
=======
    String senha
) {
    public UsuarioUpdateDTO(String nome, String email, String senha) {
        this(nome, email, null, senha);
>>>>>>> 2e41b15 (feat: add logic in backend to confirm that sent senhaAtual really is.)
=======
    String senha,

    Moeda moedaPadrao
) {
    public UsuarioUpdateDTO(String nome, String email, String senha) {
        this(nome, email, null, senha, null);
>>>>>>> 25b46ec (feat: implement user entity, service, DTOs, and currency preference features)
    }
}
