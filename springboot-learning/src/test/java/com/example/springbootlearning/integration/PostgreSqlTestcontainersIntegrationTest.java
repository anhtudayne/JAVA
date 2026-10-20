package com.example.springbootlearning.integration;

import com.example.springbootlearning.model.Product;
import com.example.springbootlearning.repository.ProductRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 📘 BÀI 15: Kiểm Thử Cơ Sở Dữ Liệu Thật với Testcontainers & @ServiceConnection (Spring Boot 3.1+)
 *
 * Điểm sáng công nghệ:
 *   - Sử dụng Docker container chứa PostgreSQL 16 thật
 *   - @ServiceConnection tự động bind DataSource properties (url, username, password)
 *     mà KHÔNG cần dùng @DynamicPropertySource thủ công
 *   - Chú thích @Disabled: Giúp test suite chạy mượt mà ngay cả khi môi trường chưa bật Docker Desktop.
 *     Khi dev bật Docker Desktop, chỉ cần bỏ chú thích @Disabled là test chạy thật 100%!
 */
@Disabled("🐳 Yêu cầu Docker Daemon đang chạy (Docker Desktop) - Bỏ @Disabled khi bật Docker để kiểm thử trên PostgreSQL thật")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("PostgreSQL Testcontainers - Kiểm Thử Database Thật")
class PostgreSqlTestcontainersIntegrationTest {

    /**
     * Khai báo PostgreSQL Container:
     * - @Container: Quản lý vòng đời start/stop theo JUnit 5
     * - @ServiceConnection: Spring Boot 3.1+ tự động inject connection info vào DataSource
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Lưu và truy vấn sản phẩm trên PostgreSQL Container thật")
    void testSaveAndFindProduct_OnRealPostgreSQL() {
        // Given
        Product product = new Product(null, "Testcontainers Product", "Chạy trên Postgres thật", 199.0, "Test", 10);

        // When
        Product saved = productRepository.save(product);

        // Then
        assertThat(saved.getId()).isNotNull();
        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Testcontainers Product");
    }
}
