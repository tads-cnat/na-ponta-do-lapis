package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Conquista;
import com.npl.na_ponta_do_lapis.entity.enums.CategoriaConquista;
import java.time.LocalDateTime;

public record ConquistaItemDTO(
        String id,
        String titulo,
        String descricao,
        String icone,
        Integer pontosXp,
        CategoriaConquista categoria,
        boolean desbloqueada,
        LocalDateTime dataDesbloqueio
) {
    public static ConquistaItemDTO fromEntity(Conquista c, boolean desbloqueada, LocalDateTime dataDesbloqueio) {
        return new ConquistaItemDTO(
                c.getId(),
                c.getTitulo(),
                c.getDescricao(),
                c.getIcone(),
                c.getPontosXp(),
                c.getCategoria(),
                desbloqueada,
                dataDesbloqueio
        );
    }
}

