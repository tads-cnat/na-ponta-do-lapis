package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.AporteMeta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AporteMetaResponseDTO(
        Long id,
        Long metaId,
        Long usuarioId,
        String usuarioNome,
        Long contaOrigemId,
        String contaOrigemNome,
        BigDecimal valor,
        String observacao,
        LocalDateTime dataAporte
) {
    public static AporteMetaResponseDTO fromEntity(AporteMeta a) {
        return new AporteMetaResponseDTO(
                a.getId(),
                a.getMeta().getId(),
                a.getUsuario().getId(),
                a.getUsuario().getNome(),
                a.getContaOrigem() != null ? a.getContaOrigem().getId() : null,
                a.getContaOrigem() != null ? a.getContaOrigem().getNome() : null,
                a.getValor(),
                a.getObservacao(),
                a.getDataAporte()
        );
    }
}

