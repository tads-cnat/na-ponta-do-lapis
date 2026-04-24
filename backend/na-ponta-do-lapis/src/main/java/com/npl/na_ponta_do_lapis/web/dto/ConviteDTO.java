package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;

public record ConviteDTO(Usuario destinatario, Familia destinatarioFamilia) {

    public Convite toEntity() {
        Convite convite = new Convite();
        convite.setDestinatario(destinatario);
        convite.setFamilia(destinatarioFamilia);
        convite.setStatus(StatusConvite.PENDENTE);
        return convite;
    }
}
