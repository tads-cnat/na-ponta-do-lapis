package com.npl.na_ponta_do_lapis.entity.enums;

public enum TipoConta {

    CREDITO("Crédito"),
    DEBITO("Débito"),
    CREDITO_DEBITO("Crédito/Débito");

    private final String tipo;

    private TipoConta(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return this.tipo;
    }
}
