package com.npl.na_ponta_do_lapis.web.exception;

/**
 * Exceção lançada quando um usuário tenta autenticar com e-mail e senha
 * antes de confirmar o endereço de e-mail cadastrado.
 */
public class EmailNaoVerificadoException extends RuntimeException {

    private final String email;

    public EmailNaoVerificadoException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}

