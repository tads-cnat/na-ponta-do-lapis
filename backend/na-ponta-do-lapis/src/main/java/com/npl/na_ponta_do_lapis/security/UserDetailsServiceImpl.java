package com.npl.na_ponta_do_lapis.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioAuthCacheService usuarioAuthCacheService;

    public UserDetailsServiceImpl(UsuarioAuthCacheService usuarioAuthCacheService) {
        this.usuarioAuthCacheService = usuarioAuthCacheService;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalized = identifier == null ? "" : identifier.strip();
        return usuarioAuthCacheService.buscarPorIdentificador(normalized)
                .orElseThrow(() -> new UsernameNotFoundException(normalized));
    }
}
