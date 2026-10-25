package com.npl.na_ponta_do_lapis.config;

import jakarta.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class TomcatRootRedirectConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatRootCustomizer() {
        return factory -> factory.addEngineValves(new ValveBase() {
            @Override
            public void invoke(Request request, Response response) throws IOException, ServletException {
                String uri = request.getRequestURI();

                // Trata acessos na raiz direta do subdominio (ex: https://api-homolog.napontadolapis.app/)
                if (uri == null || uri.equals("/") || uri.isEmpty()) {
                    response.sendRedirect("/api/docs");
                    return;
                }

                // Trata acessos a /api sem barra
                if (uri.equals("/api")) {
                    response.sendRedirect("/api/docs");
                    return;
                }

                // Se o usuario acessar /docs diretamente sem o prefixo /api
                if (uri.equals("/docs") || uri.startsWith("/docs/")) {
                    response.sendRedirect("/api" + uri);
                    return;
                }

                getNext().invoke(request, response);
            }
        });
    }
}
