package com.npl.na_ponta_do_lapis.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ApiExternaCotacaoException extends RuntimeException {
    public ApiExternaCotacaoException(String message) {
        super(message);
    }
}
