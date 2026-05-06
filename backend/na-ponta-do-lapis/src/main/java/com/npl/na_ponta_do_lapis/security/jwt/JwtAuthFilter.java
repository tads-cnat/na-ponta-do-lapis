package com.npl.na_ponta_do_lapis.security.jwt;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private UsuarioRepository usuarioRepository;
    private UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(UsuarioRepository usuarioRepository, UserDetailsServiceImpl userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.userDetailsService = userDetailsService;
    }

    public static String getEmailUsuarioLogado(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((Usuario) principal).getEmail();
        } else {
            return principal.toString();
        }
    }

    private void toAuthentication(HttpServletRequest req, String email) throws ServletException, IOException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String authorizationHeader = request.getHeader(JwtUtil.JWT_AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtUtil.JWT_BEARER)) {
            logger.error("JWT Token nulo, vazio ou não iniciado com bearer.");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(JwtUtil.JWT_BEARER.length());

        if (!JwtUtil.validateToken(token)) {
            logger.warn("JWT Token está inválido ou expirado.");

            filterChain.doFilter(request, response);
            return;
        }

        String email = JwtUtil.extractEmail(token);

        toAuthentication(request, email);
        filterChain.doFilter(request, response);
    }
}
