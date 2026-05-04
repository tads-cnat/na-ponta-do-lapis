package com.npl.na_ponta_do_lapis.security.jwt;

import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private UserDetailsService userDetailsService;

    public JwtAuthFilter(UsuarioRepository usuarioRepository, UserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.userDetailsService = userDetailsService;
    }

    private void toAuthentication(HttpServletRequest req, String email) throws ServletException, IOException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // O uso de contains ou startsWith no URI é mais seguro para o Swagger
        return path.contains("/swagger-ui")
                || path.contains("/v3/api-docs")
                || path.contains("/auth")
                || path.contains("/favicon.ico")
                || path.contains("/docs")
                || path.contains("/api-docs"); // O Swagger costuma pedir o ícone também
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String authorizationHeader = request.getHeader(JwtUtil.JWT_AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
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
