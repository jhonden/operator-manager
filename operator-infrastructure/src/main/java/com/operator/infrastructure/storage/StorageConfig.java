package com.operator.infrastructure.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Storage Configuration
 *
 * Conditionally creates storage beans based on configuration
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Configuration
public class StorageConfig {

    /**
     * MinIO Storage Service Bean
     * Only created when storage.type is 'minio' or not specified
     */
    @Bean
    @ConditionalOnProperty(name = "operator.storage.type", havingValue = "minio", matchIfMissing = true)
    public MinioStorageService minioStorageService() {
        return new MinioStorageService();
    }
}