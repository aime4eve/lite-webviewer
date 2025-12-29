package com.smartmoldguard.subscription.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI subscriptionServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Subscription Service API")
                        .description("API for SmartMoldGuard Subscription & Commercialization")
                        .version("v1.0"));
    }
}
