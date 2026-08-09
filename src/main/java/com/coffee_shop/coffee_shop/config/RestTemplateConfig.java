package com.coffee_shop.coffee_shop.config;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /*
     * RestTemplateBuilder
     *
     * - Provided by Spring Boot
     * - Used to create/configure RestTemplate
     * - build() creates the RestTemplate object
     *
     * Example:
     * builder
     *     .setConnectTimeout(...)
     *     .setReadTimeout(...)
     *     .build();
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}