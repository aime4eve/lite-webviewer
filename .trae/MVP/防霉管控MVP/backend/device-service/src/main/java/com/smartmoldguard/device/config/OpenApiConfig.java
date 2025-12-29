package com.smartmoldguard.device.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deviceServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Device Service API")
                        .description("API for SmartMoldGuard Device Service")
                        .version("v1.0"));
    }
}
