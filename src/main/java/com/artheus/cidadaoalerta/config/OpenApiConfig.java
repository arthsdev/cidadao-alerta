package com.artheus.cidadaoalerta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Configuração personalizada do OpenAPI (Swagger UI).
     * Aqui definimos as informações da API (título, versão e descrição)
     * e também o esquema de autenticação com JWT.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Informações básicas da API (aparecem no Swagger UI)
                .info(new Info()
                        .title("Cidadão Alerta API")
                        .version("1.0.0")
                        .description("Documentação da API do projeto Cidadão Alerta"))

                .components(new Components()
                        .addSecuritySchemes("bearerAuth", // Nome do esquema de segurança
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP) // Tipo HTTP (usado para JWT)
                                        .scheme("bearer") // Indica uso de Bearer Token
                                        .bearerFormat("JWT"))) // Formato do token (JWT)

                // Define como requisito de segurança o bearerAuth
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
