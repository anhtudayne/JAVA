package com.example.springbootlearning.dto.response;

import com.example.springbootlearning.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;

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
 *
 * 📘 BÀI 8 — @Schema có thể gắn trực tiếp lên từng record component.
 */
@Schema(description = "Thông tin sản phẩm trả về cho Client")
public record ProductResponse(
        @Schema(description = "ID duy nhất của sản phẩm", example = "1")
        Long id,
        @Schema(description = "Tên sản phẩm", example = "MacBook Pro M4 16 inch")
        String name,
        @Schema(description = "Mô tả sản phẩm", example = "Chip M4 Pro, RAM 32GB, SSD 1TB")
        String description,
        @Schema(description = "Giá sản phẩm (USD)", example = "2499.99")
        Double price,
        @Schema(description = "Danh mục", example = "Laptop")
        String category,
        @Schema(description = "Số lượng tồn kho", example = "15")
        Integer stock,
        @Schema(description = "Thời điểm tạo", example = "2026-10-03T14:30:00")
        LocalDateTime createdAt,
        @Schema(description = "Thời điểm cập nhật gần nhất", example = "2026-10-03T15:45:00")
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
