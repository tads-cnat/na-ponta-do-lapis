package com.npl.na_ponta_do_lapis.web.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacoesResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoTransacao tipo,
        EstadoTransacao estado,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHora,
        CategoriaResumoDTO categoria,
        ContaResumoDTO conta,
        MarcadorResumeDTO marcador
) {
       public TransacoesResponseDTO(Transacao transacao){
               this(
                       transacao.getId(),
                       transacao.getDescricao(),
                       transacao.getValor(),
                       transacao.getTipo(),
                       transacao.getEstado(),
                       transacao.getDataHora(),
                       new CategoriaResumoDTO(
                               transacao.getCategoria().getId(),
                               transacao.getCategoria().getNome()
                       ),
                       new ContaResumoDTO(
                               transacao.getContaFinanceira().getId(),
                               transacao.getContaFinanceira().getNome()
                       ),
                       transacao.getMarcador() != null ? new MarcadorResumeDTO(
                               transacao.getMarcador().getNome(),
                               transacao.getMarcador().getCor()
                       ): null
               );
       };
}

