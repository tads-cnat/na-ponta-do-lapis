package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;

public interface AuthService {

    void criarContaAdmin();

    UsuarioDTO cadastrarUsuario();

    boolean hasUserWithEmail(String email);
}
