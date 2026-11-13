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
<<<<<<< HEAD
        return usuarioAuthCacheService.buscarPorIdentificador(normalized)
=======
        return usuarioRepository.findByEmailIgnoreCase(normalized)
                .or(() -> usuarioRepository.findByUsernameIgnoreCase(normalized))
>>>>>>> ad97d07 (feat(backend): permite login por email ou username e adiciona autenticacao com Google)
                .orElseThrow(() -> new UsernameNotFoundException(normalized));
    }
}
