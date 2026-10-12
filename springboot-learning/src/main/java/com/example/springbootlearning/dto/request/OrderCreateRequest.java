package com.example.springbootlearning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 📘 BÀI 12 — Request DTO cho tạo đơn hàng (Transaction Management)
 */
@Schema(description = "Dữ liệu yêu cầu tạo mới đơn hàng")
public record OrderCreateRequest(
        @Schema(description = "ID của người dùng đặt mua", example = "1")
        @NotNull(message = "userId không được null")
        Long userId,

        @Schema(description = "ID của sản phẩm cần mua", example = "1")
        @NotNull(message = "productId không được null")
        Long productId,

        @Schema(description = "Số lượng đặt mua (1 đến 100)", example = "2")
        @NotNull(message = "quantity không được null")
        @Min(value = 1, message = "Số lượng đặt mua tối thiểu là 1")
        @Max(value = 100, message = "Số lượng đặt mua tối đa trong một lần là 100")
        Integer quantity,

        @Schema(description = "Ghi chú đơn hàng", example = "Giao giờ hành chính")
        @Size(max = 500, message = "Ghi chú đơn hàng tối đa 500 ký tự")
        String description
) {}
