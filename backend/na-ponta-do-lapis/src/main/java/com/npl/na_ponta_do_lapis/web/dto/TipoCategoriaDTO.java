package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TipoCategoriaDTO(
        @NotBlank(message = "O nome da categoria é obrigatório")
        @NotNull
        String nome
) {
    public TipoCategoria toEntity() {
        TipoCategoria entity = new TipoCategoria();
        entity.setNome(this.nome);
        return entity;
    }
}
