package com.npl.na_ponta_do_lapis.web.Controller.Exception;

public class UsuarioIdNaoExisteException extends RuntimeException {
    public UsuarioIdNaoExisteException(String message) {
        super(message);
    }
}
