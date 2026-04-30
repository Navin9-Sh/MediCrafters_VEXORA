package com.clinicalsystem.config;

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

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI clinicalSystemOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url(baseUrl).description("Current Environment"),
                        new Server().url("https://api.clinicalsystem.com").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Clinical Decision System API")
                .description("""
                        AI/ML-Powered Real-Time Clinical Decision, Analytics & Consultation System.
                        
                        **Authentication**: All endpoints (except /api/v1/auth/**) require a Bearer JWT token.
                        Obtain the token from POST /api/v1/auth/login using your Firebase ID token.
                        
                        **Rate Limiting**: 100 req/min per IP on general endpoints, 20 req/min on auth endpoints.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Clinical System Team")
                        .email("support@clinicalsystem.com"))
                .license(new License()
                        .name("Private — All Rights Reserved"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter the JWT token obtained from the /api/v1/auth/login endpoint.");
    }
}
