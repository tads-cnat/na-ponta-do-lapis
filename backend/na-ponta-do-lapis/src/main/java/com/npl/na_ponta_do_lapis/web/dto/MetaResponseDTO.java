package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Meta;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal valor,
        String fotoUrl,
        LocalDate dataInicio,
        LocalDate dataLimite,
        LocalDate dataEncerramento,
        TipoMetaResponseDTO tipoMeta
) {
    public MetaResponseDTO(Meta meta) {
        this(
                meta.getId(),
                meta.getNome(),
                meta.getDescricao(),
                meta.getValor(),
                meta.getFotoUrl(),
                meta.getDataInicio(),
                meta.getDataLimite(),
                meta.getDataEncerramento(),
                meta.getTipoMeta() != null ? new TipoMetaResponseDTO(meta.getTipoMeta()) : null
        );
    }
}