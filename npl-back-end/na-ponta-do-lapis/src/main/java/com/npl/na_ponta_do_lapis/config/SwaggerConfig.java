package com.npl.na_ponta_do_lapis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(
                        new Info()
                                .title("REST API - Na Ponta do Lápis")
                                .description("Gerenciamento de endpoints da API NPL")
                                .version("1.0")
                                .contact(new Contact().name("Na Ponta do Lápis").email("npl@gmail.com"))
                );
    }

//    private SecurityScheme securityScheme() {
//        return new SecurityScheme()
//                .description("Insira um Bearer token valido para prosseguir")
//                .type(SecurityScheme.Type.HTTP)
//                .in(SecurityScheme.In.HEADER)
//                .scheme("Bearer")
//                .bearerFormat("JWT")
//                .name("Security");
//
//    }
}
