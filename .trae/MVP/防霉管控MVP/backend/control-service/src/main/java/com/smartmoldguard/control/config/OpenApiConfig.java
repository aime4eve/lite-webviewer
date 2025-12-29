package com.smartmoldguard.control.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI controlServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Control Service API")
                        .description("API for SmartMoldGuard Control Service")
                        .version("v1.0"));
    }
}
