package com.npl.na_ponta_do_lapis.web.Controller.dto;

import com.npl.na_ponta_do_lapis.entity.Familia;

public record FamiliaResponseDTO(Long id, String nome, String foto) {

    public FamiliaResponseDTO(Familia familia){
        this(familia.getId(), familia.getNome(), familia.getFotoFamilia());
    }
}
