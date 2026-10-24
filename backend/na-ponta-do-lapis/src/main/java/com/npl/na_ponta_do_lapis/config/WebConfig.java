package com.npl.na_ponta_do_lapis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Adiciona o prefixo /v1 apenas aos RestControllers da aplicacao
        // Actuator (/api/actuator) e Swagger (/api/docs) continuam em seus caminhos padrao
        configurer.addPathPrefix("/v1",
                HandlerTypePredicate.forBasePackage("com.npl.na_ponta_do_lapis")
                        .and(HandlerTypePredicate.forAnnotation(RestController.class)));
    }
}
