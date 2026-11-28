package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;

import java.time.LocalDateTime;

public record ChaveIaResponseDTO(
        Long id,
        String apelido,
        ProvedorIa provedor,
        String nomeProvedor,
        String chaveMascarada,
        boolean padrao,
        boolean suportaPdf,
        LocalDateTime criadoEm,
        String modeloAtivo
) {
    public ChaveIaResponseDTO(
            Long id,
            String apelido,
            ProvedorIa provedor,
            String nomeProvedor,
            String chaveMascarada,
            boolean padrao,
            boolean suportaPdf,
            LocalDateTime criadoEm
    ) {
        this(id, apelido, provedor, nomeProvedor, chaveMascarada, padrao, suportaPdf, criadoEm, null);
    }
}

