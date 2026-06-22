package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

public record MetaResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal valorTotal,
        BigDecimal valorAtual,
        String fotoUrl,
        LocalDate dataLimite,
        TipoMeta tipoMeta,
        Long usuarioId,
        Long contaId,
        double progresso 
) {
    public MetaResponseDTO(Meta meta, double progressoCalculado) {
        this(
            meta.getId(),
            meta.getNome(),
            meta.getDescricao(),
            meta.getValor(),
            meta.getValorAtual(),
            meta.getFotoUrl(),
            meta.getDataLimite(),
            meta.getTipoMeta(),
            meta.getUsuario() != null ? meta.getUsuario().getId() : null,
            meta.getConta() != null ? meta.getConta().getId() : null,
            progressoCalculado
        );
    }
}