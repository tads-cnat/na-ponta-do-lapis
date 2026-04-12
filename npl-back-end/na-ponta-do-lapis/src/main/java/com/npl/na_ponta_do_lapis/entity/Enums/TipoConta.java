package com.npl.na_ponta_do_lapis.entity.Enums;

public enum TipoConta {

    CREDITO("Credito"),
    DEBITO("Debito"),
    CREDITO_DEBITO("Credito_Debito");

    private String tipo;

    private TipoConta(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return "ROLE_"+this.tipo;
    }
}
