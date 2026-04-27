package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Marcador;

public record MarcadorDTO(String nome, String cor) {

    public Marcador toEntity(){
        Marcador marcador = new Marcador();
        marcador.setNome(nome);
        marcador.setCor(cor);
        return marcador;
    }
}
