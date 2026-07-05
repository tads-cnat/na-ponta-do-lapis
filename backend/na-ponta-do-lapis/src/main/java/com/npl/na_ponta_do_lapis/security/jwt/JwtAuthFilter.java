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

    private JwtUtil jwtUtil;
    private UsuarioRepository usuarioRepository;
    private UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.userDetailsService = userDetailsService;
    }

    public static String getEmailUsuarioLogado(){
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof Usuario) {
                return ((Usuario) principal).getEmail();
            } else if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal != null) {
                return principal.toString();
            }
        } catch (Exception e) {
            System.err.println("Erro ao extrair email do usuário logado: " + e.getMessage());
        }
        return null;
    }

    private void toAuthentication(HttpServletRequest req, String email) throws ServletException, IOException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }


        String authorizationHeader = request.getHeader(JwtUtil.JWT_AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtUtil.JWT_BEARER)) {
            logger.error("JWT Token nulo, vazio ou não iniciado com bearer.");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(JwtUtil.JWT_BEARER.length());

        if (!jwtUtil.validateToken(token)) {
            logger.warn("JWT Token está inválido ou expirado.");

            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);

        toAuthentication(request, email);
        filterChain.doFilter(request, response);
    }
}
