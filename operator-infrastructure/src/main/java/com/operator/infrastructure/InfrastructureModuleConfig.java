package com.operator.infrastructure;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Infrastructure Module Configuration
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@EnableScheduling
@EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class})
public class InfrastructureModuleConfig {
}
