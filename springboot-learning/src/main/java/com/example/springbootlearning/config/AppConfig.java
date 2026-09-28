package com.example.springbootlearning.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Minh hoạ @Configuration và @Bean:
 * Dùng khi muốn đăng ký Bean từ thư viện bên ngoài (3rd party)
 * hoặc khi việc khởi tạo Bean cần logic cấu hình tuỳ chỉnh phức tạp.
 */
@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public DateTimeFormatter appDateFormatter() {
        log.info("⚙️ [AppConfig] Đăng ký bean DateTimeFormatter (pattern: yyyy-MM-dd HH:mm:ss)...");
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Bean("systemSignature")
    public String systemSignature() {
        log.info("⚙️ [AppConfig] Đăng ký bean systemSignature...");
        return "Powered by Spring Boot 3 - Antigravity Learning System";
    }
}
