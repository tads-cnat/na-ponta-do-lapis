package com.npl.na_ponta_do_lapis.web.Controller.Exception;

public class TransacaoNaoExisteException extends RuntimeException {
    public TransacaoNaoExisteException(String message) {
        super(message);
    }
}
