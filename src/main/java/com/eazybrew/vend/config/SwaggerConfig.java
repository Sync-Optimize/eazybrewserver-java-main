package com.eazybrew.vend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server.production.url:http://159.65.181.236:9090}")
    private String productionUrl;

    @Value("${swagger.server.development.url:http://localhost:9090}")
    private String developmentUrl;

    @Value("${swagger.server.local.url:http://localhost:9090}")
    private String localUrl;

    @Bean
    public OpenAPI customOpenAPI() {

        List<Server> servers = new ArrayList<>();

        // Add production server first so it's selected by default
        servers.add(new Server()
                .url(productionUrl)
                .description("Production Server"));

        // Add development server as an option
        servers.add(new Server()
                .url(developmentUrl)
                .description("Development Server"));

        // Add development server as an option
        servers.add(new Server()
                .url(localUrl)
                .description("Local Server"));

        return new OpenAPI()
                .info(new Info()
                        .title("Eazy Vend API")
                        .version("1.0")
                        .description("Eazy Vend Application API Documentation")
                        .contact(new Contact()
                                .name("Eazy Brew")
                                .email("support@eazybrew.com")
                                .url("https://eazybrew.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication")));
    }
}