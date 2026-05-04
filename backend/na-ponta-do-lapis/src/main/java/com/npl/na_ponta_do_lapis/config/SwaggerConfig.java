package com.npl.na_ponta_do_lapis.config;



import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 1. OBRIGATÓRIO: Diz ao Swagger para aplicar a segurança em todos os endpoints
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                // 2. Define o esquema de segurança
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("REST API - Na Ponta do Lápis")
                        .version("1.0")
                        .description("Gerenciamento de endpoints da API NPL"));
    }
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .description("Insira um Bearer token valido para prosseguir")
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .scheme("Bearer")
                .bearerFormat("JWT")
                .name("Security");

    }
}
