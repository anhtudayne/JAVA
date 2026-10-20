package com.example.springbootlearning.dto.external;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 📘 BÀI 16 — DTO gửi request tạo hoặc cập nhật bài viết lên External API
 */
public record ExternalPostCreateRequest(
        @NotBlank(message = "Tiêu đề bài viết không được để trống")
        String title,

        @NotBlank(message = "Nội dung bài viết không được để trống")
        String body,

        @NotNull(message = "ID tác giả (userId) không được để trống")
        @Positive(message = "userId phải lớn hơn 0")
        Long userId
) {}
