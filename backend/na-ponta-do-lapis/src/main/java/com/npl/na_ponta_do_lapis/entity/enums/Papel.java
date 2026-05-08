package com.npl.na_ponta_do_lapis.entity.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Papel implements GrantedAuthority {
    USUARIO,
    ADMIN_FAMILIA,
    ADMIN_SITE;

    private String autoridade;


    @Override
    public String getAuthority() {
        return "ROLE_"+this.name();
    }
}
