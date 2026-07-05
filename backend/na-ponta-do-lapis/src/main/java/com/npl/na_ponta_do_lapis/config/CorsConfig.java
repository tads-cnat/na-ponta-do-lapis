package com.npl.na_ponta_do_lapis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    
    // Lê o valor do application.properties ou do .env (CORS_ALLOWED_ORIGINS)
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Quebra a string de URLs por vírgula e aplica como origens permitidas
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(","))); 

        // Métodos HTTP permitidos (mantive o PATCH que você já tinha)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));

        // Permitir envio de credenciais (cookies, Authorization headers para o JWT)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica para todas as rotas (incluindo o /api que você configurou no properties)
        source.registerCorsConfiguration("/**", configuration); 
        return source;
    }
}