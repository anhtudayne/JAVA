package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 📘 BÀI 15: Slice Testing với @DataJpaTest
 *
 * Đặc điểm:
 *   - Chỉ nạp các JPA components (Entities, Repositories, EntityManager, DataSource)
 *   - Không nạp Web Controllers, Services, Security beans
 *   - Sử dụng TestEntityManager để chuẩn bị và flush dữ liệu xuống database
 *   - Tự động rollback sau mỗi test method
 */
@DataJpaTest
@DisplayName("UserRepository - Slice Test với @DataJpaTest")
class UserRepositoryDataJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Tìm user theo email không phân biệt hoa thường thành công")
    void testFindByEmailIgnoreCase_Success() {
        // Given: Tạo và lưu user bằng TestEntityManager
        User user = new User(null, "Nguyen Van A", "nguyenvana@example.com");
        entityManager.persistAndFlush(user);
        entityManager.clear(); // Xóa 1st-level cache để kiểm thử câu SELECT SQL thật sự

        // When: Tìm kiếm bằng chữ hoa/thường lẫn lộn
        Optional<User> found = userRepository.findByEmailIgnoreCase("NGUYENVANA@EXAMPLE.COM");

        // Then: Kiểm chứng kết quả
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Nguyen Van A");
        assertThat(found.get().getEmail()).isEqualTo("nguyenvana@example.com");
    }

    @Test
    @DisplayName("Kiểm tra tồn tại email (existsByEmail) trả về true khi email đã có")
    void testExistsByEmail_True() {
        // Given
        User user = new User(null, "Tran Thi B", "tranthib@example.com");
        entityManager.persistAndFlush(user);
        entityManager.clear();

        // When & Then
        boolean exists = userRepository.existsByEmail("tranthib@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Lưu mới User và tự sinh ID tự tăng")
    void testSaveUser_GeneratesId() {
        // Given
        User newUser = new User(null, "Le Van C", "levanc@example.com");

        // When
        User saved = userRepository.save(newUser);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Le Van C");
    }
}
