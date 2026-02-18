package com.operator.infrastructure.config;

import com.operator.common.enums.UserRole;
import com.operator.common.enums.UserStatus;
import com.operator.core.operator.repository.OperatorRepository;
import com.operator.core.security.domain.User;
import com.operator.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * Data Initializer
 *
 * Creates default admin user for development
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OperatorRepository operatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    public void run(String... args) {
        // Check if admin user exists
        if (userRepository.count() == 0) {
            log.info("No users found. Creating default admin user...");

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@operator.local")
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created: username=admin, password=admin123");
        } else {
            log.debug("Users already exist. Skipping admin user creation.");
        }

        // Fix invalid language values in operators
        try {
            Query query = entityManager.createNativeQuery(
                "UPDATE operators SET language = 'JAVA' WHERE language = '0' OR language IS NULL OR language = ''"
            );
            int updatedCount = query.executeUpdate();
            if (updatedCount > 0) {
                log.info("Fixed {} operators with invalid language values", updatedCount);
            }
        } catch (Exception e) {
            log.warn("Failed to fix invalid language values: {}", e.getMessage());
        }
    }
}
