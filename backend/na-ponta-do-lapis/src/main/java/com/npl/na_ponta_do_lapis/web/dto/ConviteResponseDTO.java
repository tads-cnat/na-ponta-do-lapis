package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;

public record ConviteResponseDTO(Long id, Usuario destinatario, Familia familia, StatusConvite status) {

    public ConviteResponseDTO(Convite convite){this(convite.getId(), convite.getDestinatario(), convite.getFamilia(), convite.getStatus());}
}
