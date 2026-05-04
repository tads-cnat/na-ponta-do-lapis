package com.npl.na_ponta_do_lapis.entity.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Papel implements GrantedAuthority {
    USUARIO("Usuario"),
    ADMIN_FAMILIA("Admin_Familia"),
    ADMIN_SITE("Admin_Site");

    private String autoridade;

    Papel(String autoridade){
        this.autoridade = autoridade;
    }

    public String getAutoridade() {
        return "ROLE_"+autoridade;
    }

    @Override
    public String getAuthority() {
        return autoridade;
    }
}
