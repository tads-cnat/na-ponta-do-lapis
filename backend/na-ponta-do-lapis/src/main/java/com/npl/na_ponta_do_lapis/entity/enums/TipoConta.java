package com.npl.na_ponta_do_lapis.entity.enums;

public enum TipoConta {

<<<<<<< HEAD
    CREDITO("Crédito"),
    DEBITO("Débito"),
    CREDITO_DEBITO("Crédito/Débito");

    private final String tipo;
=======
    CREDITO("Credito"),
    DEBITO("Debito"),
    CREDITO_DEBITO("Credito Debito");

    private String tipo;
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)

    private TipoConta(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return this.tipo;
    }
}
