package com.travel.travelecosystem.infrastructure.config;

import com.travel.travelecosystem.infrastructure.persistence.entity.UserEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.admin.enabled", havingValue = "true", matchIfMissing = true)
public class AdminUserInitializer implements ApplicationRunner {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.first-name:System}")
    private String adminFirstName;

    @Value("${app.admin.last-name:Administrator}")
    private String adminLastName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userJpaRepository.existsByEmail(normalizedEmail)) {
            log.info("Аккаунт администратора уже существует. Email для входа: {}", normalizedEmail);
            return;
        }

        UserEntity admin = UserEntity.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .firstName(adminFirstName.trim())
                .lastName(adminLastName.trim())
                .role(UserEntity.UserRole.ROLE_ADMIN)
                .status(UserEntity.UserStatus.ACTIVE)
                .build();
        userJpaRepository.save(admin);

        log.warn("""
                
                ========== АККАУНТ АДМИНИСТРАТОРА СОЗДАН ==========
                Email:    {}
                Password: {}
                Роль:     ROLE_ADMIN
                Вход:     POST /api/auth/login
                ================================================
                """, normalizedEmail, adminPassword);
    }
}
