package com.example.springbootlearning.dto.response;

import com.example.springbootlearning.model.Product;

import java.time.LocalDateTime;

/**
 * 📘 BÀI 5 — Response DTO trả về cho Client (Java Record)
 *
 * Dùng Java Record (JDK 16+) để viết DTO siêu gọn:
 *   - Tự sinh: constructor, getters (id(), name()...), equals, hashCode, toString
 *   - Immutable (không có setter) → an toàn, phù hợp cho Response DTO
 *
 * Chỉ chứa field AN TOÀN — ẩn các field nhạy cảm/nội bộ.
 *
 * 🔑 Record getter KHÔNG có prefix "get":
 *   - product.id()       thay vì product.getId()
 *   - product.name()     thay vì product.getName()
 *
 * Jackson hỗ trợ serialize Record → JSON natively từ Spring Boot 2.7+
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        Double price,
        String category,
        Integer stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Static Factory Method — Chuyển Product Entity → ProductResponse DTO
     *
     * Đây là nơi DUY NHẤT thực hiện chuyển đổi Entity → DTO.
     * Nếu sau này thêm/bớt field trong Entity, chỉ cần sửa method này.
     *
     * @param product Product entity từ tầng Service/Repository
     * @return ProductResponse DTO chỉ chứa field an toàn
     */
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
