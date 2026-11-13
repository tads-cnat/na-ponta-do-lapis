package com.npl.na_ponta_do_lapis.security;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalized = identifier == null ? "" : identifier.strip();
        return usuarioRepository.findByEmailIgnoreCase(normalized)
                .or(() -> usuarioRepository.findByUsernameIgnoreCase(normalized))
                .orElseThrow(() -> new UsernameNotFoundException(normalized));
    }
}
