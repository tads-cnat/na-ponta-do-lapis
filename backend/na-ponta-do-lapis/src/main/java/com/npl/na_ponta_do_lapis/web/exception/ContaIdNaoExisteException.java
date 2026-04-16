package com.npl.na_ponta_do_lapis.web.exception;

<<<<<<< HEAD
<<<<<<< HEAD
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
public class ContaIdNaoExisteException extends RuntimeException {
    public ContaIdNaoExisteException(String message) {
        super(message);
    }
}
