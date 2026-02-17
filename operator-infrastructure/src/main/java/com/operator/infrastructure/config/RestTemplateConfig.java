package com.operator.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Infrastructure Configuration
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Configure RestTemplate for HTTP requests
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(30000);    // 30 seconds

        return new RestTemplate(factory);
    }
}
