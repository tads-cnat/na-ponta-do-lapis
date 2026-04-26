package com.npl.na_ponta_do_lapis.web.exception;

public class MetaNaoEncontradaException extends NotFoundException {

    public MetaNaoEncontradaException() {
        super("Meta não encontrada");
    }

}
