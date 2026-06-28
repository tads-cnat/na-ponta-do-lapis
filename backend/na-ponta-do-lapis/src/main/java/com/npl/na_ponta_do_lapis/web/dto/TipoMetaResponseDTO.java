package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de meta disponível")
public record TipoMetaResponseDTO(
        @Schema(example = "POUPANCA")
        String id,
        @Schema(example = "Poupança")
        String nome
) {
    public static TipoMetaResponseDTO fromEnum(TipoMeta tipo) {
        return new TipoMetaResponseDTO(tipo.name(), tipo.getNome());
    }
}
