package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

import java.math.BigDecimal;
import java.time.LocalDate;


public record MetaResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal valorTotal,
        BigDecimal valorAtual,
        String fotoUrl,
        LocalDate dataLimite,
        TipoMeta tipoMeta,
        com.npl.na_ponta_do_lapis.entity.enums.StatusMeta status,
        Long usuarioId,
        String usuarioNome,
        Long contaId,
        String contaNome,
        Long categoriaId,
        String categoriaNome,
        Long familiaId,
        boolean familiar,
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
            meta.getStatus(),
            meta.getUsuario() != null ? meta.getUsuario().getId() : null,
            meta.getUsuario() != null ? meta.getUsuario().getNome() : null,
            meta.getConta() != null ? meta.getConta().getId() : null,
            meta.getConta() != null ? meta.getConta().getNome() : null,
            meta.getCategoria() != null ? meta.getCategoria().getId() : null,
            meta.getCategoria() != null ? meta.getCategoria().getNome() : null,
            meta.getFamilia() != null ? meta.getFamilia().getId() : null,
            meta.getFamilia() != null,
            progressoCalculado
        );
    }
}