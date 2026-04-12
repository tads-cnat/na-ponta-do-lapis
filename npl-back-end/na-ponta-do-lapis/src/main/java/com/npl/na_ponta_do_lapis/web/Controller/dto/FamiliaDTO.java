package com.npl.na_ponta_do_lapis.web.Controller.dto;

import com.npl.na_ponta_do_lapis.entity.Familia;

public record FamiliaDTO(String nome, String foto) {

    public Familia toEntity(){
        Familia familia = new Familia();
        familia.setNome(nome);
        familia.setFotoFamilia(foto);
        return familia;
    }
}
