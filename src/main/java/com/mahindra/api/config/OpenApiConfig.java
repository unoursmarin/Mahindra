package com.mahindra.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mahindraOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mahindra Geo API")
                        .description("REST API for browsing countries and their cities. " +
                                "Supports paginated city listings and city detail lookups.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mahindra Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server")
                ));
    }
}
