package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;

public record ConviteResponseDTO(
        Long id,
        UsuarioResponseDTO destinatario,
        FamiliaResponseDTO familia,
        StatusConvite status
) {

    public ConviteResponseDTO(Convite convite) {
        this(
                convite.getId(),
                convite.getDestinatario() != null ? new UsuarioResponseDTO(convite.getDestinatario()) : null,
                convite.getFamilia() != null ? new FamiliaResponseDTO(convite.getFamilia()) : null,
                convite.getStatus()
        );
    }
}
