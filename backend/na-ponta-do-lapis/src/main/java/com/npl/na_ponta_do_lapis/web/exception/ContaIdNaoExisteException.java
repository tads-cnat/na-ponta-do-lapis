package com.npl.na_ponta_do_lapis.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ContaIdNaoExisteException extends RuntimeException {
    public ContaIdNaoExisteException(String message) {
        super(message);
    }
}
