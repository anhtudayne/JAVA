package com.example.springbootlearning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 📘 BÀI 11 — DTO chuẩn hóa kết quả phân trang (PagedResponse)
 *
 * Đóng gói metadata phân trang độc lập với Spring Data JPA framework:
 * - Tránh lộ trực tiếp cấu trúc nội bộ của interface org.springframework.data.domain.Page
 * - Chuẩn hóa tên trường rõ ràng cho Frontend (content, pageNumber, pageSize, totalPages, totalElements...)
 * - Tương thích hoàn hảo với generic wrapper ApiResponse<PagedResponse<T>>
 *
 * @param <T> Kiểu dữ liệu của phần tử trong trang (thường là Response DTO)
 */
@Schema(description = "Wrapper chuẩn cho kết quả phân trang trả về cho Client")
public record PagedResponse<T>(

        @Schema(description = "Danh sách bản ghi của trang hiện tại")
        List<T> content,

        @Schema(description = "Chỉ số trang hiện tại (0-indexed)", example = "0")
        int pageNumber,

        @Schema(description = "Số lượng bản ghi tối đa mỗi trang", example = "10")
        int pageSize,

        @Schema(description = "Tổng số bản ghi thỏa mãn điều kiện lọc", example = "100")
        long totalElements,

        @Schema(description = "Tổng số trang", example = "10")
        int totalPages,

        @Schema(description = "Có phải trang đầu tiên hay không", example = "true")
        boolean isFirst,

        @Schema(description = "Có phải trang cuối cùng hay không", example = "false")
        boolean isLast,

        @Schema(description = "Trang hiện tại có rỗng hay không", example = "false")
        boolean isEmpty
) {

    /**
     * Static Factory Method chuyển đổi từ Spring Page<T> sang PagedResponse<T>.
     *
     * @param page Đối tượng Page từ Repository/Service
     * @param <T> Kiểu dữ liệu phần tử
     * @return PagedResponse đóng gói sạch đẹp
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
