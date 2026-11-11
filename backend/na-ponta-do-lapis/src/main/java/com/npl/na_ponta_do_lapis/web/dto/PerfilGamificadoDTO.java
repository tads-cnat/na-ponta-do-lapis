package com.npl.na_ponta_do_lapis.web.dto;

import java.util.List;

public record PerfilGamificadoDTO(
        int totalXp,
        int nivel,
        String tituloNivel,
        int xpAtualNoNivel,
        int xpNecessarioParaProximoNivel,
        double progressoNivel,
        int totalMetasConcluidas,
        int totalConquistasDesbloqueadas,
        int totalConquistasExistentes,
        List<ConquistaItemDTO> conquistas
) {
}

