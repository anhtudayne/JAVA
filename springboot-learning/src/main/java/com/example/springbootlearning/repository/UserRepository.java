package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 📘 BÀI 9 — User Repository (Spring Data JPA)
 *
 * Kế thừa JpaRepository<User, Long> để thực hiện CRUD tự động trên bảng `users`.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo email (không phân biệt hoa thường).
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Kiểm tra email đã tồn tại hay chưa.
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra email không phân biệt hoa thường.
     */
    boolean existsByEmailIgnoreCase(String email);
}
