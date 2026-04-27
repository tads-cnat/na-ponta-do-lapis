package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Marcador;

public record MarcadorResponseDTO(Long id, String nome, String cor) {

    public MarcadorResponseDTO(Marcador marcador){
        this(
          marcador.getId(),marcador.getNome(), marcador.getCor()
        );
    }
}
