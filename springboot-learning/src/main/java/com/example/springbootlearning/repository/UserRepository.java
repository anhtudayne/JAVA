package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 📘 BÀI 10 & 11 — User Repository (Spring Data JPA & Specifications)
 *
 * Mở rộng:
 *   - JOIN FETCH (JPQL): Tải User cùng danh sách Orders trong 1 câu SQL duy nhất.
 *   - @EntityGraph: Giải pháp nạp EAGER linh hoạt theo từng method mà không đổi cấu hình Entity.
 *   - JpaSpecificationExecutor: Dynamic query và phân trang linh hoạt.
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

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

    // ========================================================
    // 🚀 GIẢI PHÁP DIỆT N+1 QUERY BẰNG JOIN FETCH (JPQL)
    // ========================================================

    /**
     * Nạp User cùng danh sách Orders của User đó trong duy nhất 1 câu SQL JOIN.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);

    /**
     * Lấy toàn bộ danh sách User kèm Orders (dùng DISTINCT để khử trùng lặp).
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.orders")
    List<User> findAllUsersWithOrders();

    // ========================================================
    // 🚀 GIẢI PHÁP DIỆT N+1 QUERY BẰNG @EntityGraph
    // ========================================================

    /**
     * Dùng @EntityGraph ghi đè FetchType.LAZY thành EAGER chỉ cho method này.
     */
    @EntityGraph(attributePaths = {"orders"})
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithOrdersGraph(@Param("email") String email);

    /**
     * Áp dụng @EntityGraph trực tiếp trên Derived Query method mà không cần viết JPQL.
     */
    @EntityGraph(attributePaths = {"orders"})
    List<User> findByNameContainingIgnoreCase(String name);
}
