package com.example.springbootlearning.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 📘 BÀI 16 — DTO nhận phản hồi từ JSONPlaceholder API (https://jsonplaceholder.typicode.com/posts)
 *
 * Sử dụng Java Record để tạo đối tượng bất biến (Immutable Data Carrier).
 * @JsonIgnoreProperties(ignoreUnknown = true) để an toàn nếu server ngoài trả thêm trường mới.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalPostResponse(
        Long id,
        Long userId,
        String title,
        String body
) {}
