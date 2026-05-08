package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.TipoMeta;

public record TipoMetaResponseDTO(
        Long id,
        String nome
) {

    public TipoMetaResponseDTO(TipoMeta tipoMeta) {
        this(tipoMeta.getId(), tipoMeta.getNome());
    }
}