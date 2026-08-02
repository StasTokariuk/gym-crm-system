package com.gym.crm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gymCrmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym CRM System REST API")
                        .description("REST API documentation for the Gym CRM application.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gym Admin")
                                .url("http://gym-crm.com")
                                .email("admin@gym-crm.com")));
    }
}