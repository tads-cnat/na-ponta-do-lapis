
package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Transacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoFamiliaDTO(
    Long id,
    String descricao,
    BigDecimal valor,
    String categoria,
    String estado,
    String tipo,
    LocalDateTime dataHora,
    String marcador,
    String corMarcador,
    String membroNome,
    String membroUsername
) {
    public TransacaoFamiliaDTO(Transacao t) {
        this(
            t.getId(),
            t.getDescricao(),
            t.getValor(),
            t.getCategoria().getNome(),
            t.getEstado().toString(),
            t.getTipo().toString(),
            t.getDataHora(),
            t.getMarcador() != null ? t.getMarcador().getNome() : null,
            t.getMarcador() != null ? t.getMarcador().getCor() : null,
            t.getContaFinanceira().getUsuario().getNome(),
            t.getContaFinanceira().getUsuario().getUsername()
        );
    }
}
