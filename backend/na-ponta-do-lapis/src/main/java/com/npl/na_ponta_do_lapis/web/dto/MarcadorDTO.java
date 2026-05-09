package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Marcador;

public record MarcadorDTO(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 70, message = "O nome deve ter no máximo 70 caracteres.")
        String nome,

        @NotBlank(message = "A cor é obrigatória.")
        @Pattern(regexp = "^#[0-9-A-Fa-f]{6}$", message = "A cor deve ser um HEX válido ex: #FF0000.")
        @Size(max = 7)
        String cor
) {

    public Marcador toEntity(){
        Marcador marcador = new Marcador();
        marcador.setNome(nome);
        marcador.setCor(cor);
        return marcador;
    }
}
