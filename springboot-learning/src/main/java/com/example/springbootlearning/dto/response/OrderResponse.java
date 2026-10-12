package com.example.springbootlearning.dto.response;

import com.example.springbootlearning.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 📘 BÀI 12 — Response DTO trả về thông tin đơn hàng cho Client (Java Record)
 */
@Schema(description = "Thông tin chi tiết đơn hàng trả về cho Client")
public record OrderResponse(
        @Schema(description = "Mã định danh đơn hàng", example = "1")
        Long id,

        @Schema(description = "Mô tả đơn hàng", example = "Đơn hàng MacBook Pro M3 x2")
        String description,

        @Schema(description = "Tổng giá trị đơn hàng (USD)", example = "4999.98")
        Double totalAmount,

        @Schema(description = "Trạng thái đơn hàng", example = "CONFIRMED")
        String status,

        @Schema(description = "Thời điểm khởi tạo đơn hàng", example = "2026-10-12T01:30:00")
        LocalDateTime createdAt,

        @Schema(description = "ID khách hàng", example = "1")
        Long userId,

        @Schema(description = "Họ tên khách hàng", example = "Nguyễn Văn A")
        String userName,

        @Schema(description = "Email khách hàng", example = "a@gmail.com")
        String userEmail
) {

    /**
     * Static Factory Method — Chuyển đổi an toàn từ Order Entity sang OrderResponse DTO
     * Tránh các lỗi Circular Dependency và LazyInitializationException khi trả về qua JSON.
     */
    public static OrderResponse fromEntity(Order order) {
        Long userId = null;
        String userName = null;
        String userEmail = null;

        if (order.getUser() != null) {
            userId = order.getUser().getId();
            userName = order.getUser().getName();
            userEmail = order.getUser().getEmail();
        }

        return new OrderResponse(
                order.getId(),
                order.getDescription(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                userId,
                userName,
                userEmail
        );
    }
}
