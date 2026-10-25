package com.npl.na_ponta_do_lapis.config;

import jakarta.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class TomcatRootRedirectConfig {

    @Value("${springdoc.swagger-ui.enabled:true}")
    private boolean swaggerEnabled;

    @Value("${app.web-url:https://napontadolapis.app}")
    private String webUrl;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatRootCustomizer() {
        return factory -> factory.addEngineValves(new ValveBase() {
            @Override
            public void invoke(Request request, Response response) throws IOException, ServletException {
                String uri = request.getRequestURI();
                String target = swaggerEnabled ? "/api/docs" : webUrl;

                // Trata acessos na raiz direta do subdominio (ex: https://api.napontadolapis.app/)
                if (uri == null || uri.equals("/") || uri.isEmpty() || uri.equals("/api")) {
                    response.sendRedirect(target);
                    return;
                }

                // Trata acessos diretos a /docs sem o prefixo /api
                if (uri.equals("/docs") || uri.startsWith("/docs/")) {
                    if (swaggerEnabled) {
                        response.sendRedirect("/api" + uri);
                    } else {
                        response.sendRedirect(webUrl);
                    }
                    return;
                }

                getNext().invoke(request, response);
            }
        });
    }
}
