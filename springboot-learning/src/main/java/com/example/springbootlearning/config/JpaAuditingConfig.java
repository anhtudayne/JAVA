package com.example.springbootlearning.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * 📘 BÀI 11 — Cấu hình JPA Auditing
 *
 * Kích hoạt tính năng tự động ghi nhận createdAt, updatedAt, createdBy, lastModifiedBy.
 * Khi tích hợp Spring Security (ở các bài sau), ta sẽ lấy username từ SecurityContext.
 * Hiện tại, cấu hình cung cấp giá trị mặc định là "system_admin".
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    private static final Logger log = LoggerFactory.getLogger(JpaAuditingConfig.class);

    @Bean
    public AuditorAware<String> auditorAware() {
        log.info("⚙️ [JpaAuditingConfig] Kích hoạt Spring Data JPA Auditing với AuditorAware mặc định ('system_admin')...");
        return () -> Optional.of("system_admin");
    }
}
