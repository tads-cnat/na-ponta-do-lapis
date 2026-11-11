package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Notificacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        String titulo,
        String mensagem,
        TipoNotificacao tipo,
        boolean lida,
        String linkRedirecionamento,
        LocalDateTime dataCriacao
) {
    public static NotificacaoResponseDTO fromEntity(Notificacao n) {
        return new NotificacaoResponseDTO(
                n.getId(),
                n.getTitulo(),
                n.getMensagem(),
                n.getTipo(),
                n.isLida(),
                n.getLinkRedirecionamento(),
                n.getDataCriacao()
        );
    }
}

