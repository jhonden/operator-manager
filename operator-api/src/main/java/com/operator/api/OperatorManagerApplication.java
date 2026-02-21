package com.operator.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Code Operator Management System Application
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.operator")
@EntityScan(basePackages = {
    "com.operator.core.security.domain",
    "com.operator.core.operator.domain",
    "com.operator.core.pkg.domain",
    "com.operator.core.publish.domain",
    "com.operator.core.library.domain"
})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
    "com.operator.core.security.repository",
    "com.operator.core.operator.repository",
    "com.operator.core.pkg.repository",
    "com.operator.core.publish.repository",
    "com.operator.core.library.repository"
})
public class OperatorManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperatorManagerApplication.class, args);
    }
}
