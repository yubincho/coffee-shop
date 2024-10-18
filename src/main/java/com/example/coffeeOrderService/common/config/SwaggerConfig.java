package com.example.coffeeOrderService.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(@Value("${springdoc.version}") String openApiVersion) {

        Info info = new Info()
                .title("Coffee Order Service Project")
                .version(openApiVersion)
                .description("Coffee Order Service API 명세서");

        return new OpenAPI().info(info);
    }
}
