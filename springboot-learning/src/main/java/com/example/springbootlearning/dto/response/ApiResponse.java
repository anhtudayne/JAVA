package com.example.springbootlearning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 📘 BÀI 5 — Standardized API Response Wrapper
 *
 * Mọi API response đều trả về cùng cấu trúc thống nhất:
 * {
 *     "status": 200,
 *     "message": "Thành công",
 *     "data": { ... },
 *     "timestamp": "2024-09-28T16:00:00"
 * }
 *
 * 🔑 Kiến thức Java Core:
 *   - Generics <T>: Cho phép `data` là bất kỳ kiểu nào (Product, List<Product>, String...)
 *   - Static Factory Method: Tạo object qua tên rõ nghĩa (success, created, error)
 *   - Wildcard <?> : Dùng khi data là null (error response)
 *
 * Frontend chỉ cần 1 cách xử lý:
 *   1. Kiểm tra response.status
 *   2. Đọc response.message hiển thị cho user
 *   3. Lấy response.data để render
 *
 * @param <T> Kiểu dữ liệu của field data
 *
 * 📘 BÀI 8 — springdoc tự giải generic: ResponseEntity<ApiResponse<ProductResponse>>
 *   → sinh schema riêng "ApiResponseProductResponse" với data = ProductResponse.
 */
@Schema(description = "Wrapper chuẩn cho mọi response của API")
public class ApiResponse<T> {

    @Schema(description = "HTTP status code", example = "200")
    private int status;

    @Schema(description = "Thông điệp mô tả kết quả", example = "Thành công")
    private String message;

    @Schema(description = "Dữ liệu trả về (null khi lỗi, hoặc map lỗi từng field khi validation fail)")
    private T data;

    @Schema(description = "Thời điểm server tạo response", example = "2026-10-03T14:30:00")
    private LocalDateTime timestamp;

    // ===== Private Constructor — Chỉ tạo qua Static Factory Methods =====
    // Lý do: Kiểm soát cách tạo object, đảm bảo timestamp luôn được set
    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ================================================================
    // STATIC FACTORY METHODS — Cách tạo ApiResponse rõ ràng, dễ đọc
    // ================================================================

    /**
     * Tạo response thành công (200 OK) với data.
     *
     * Ví dụ: ApiResponse.success(productResponse)
     * → {"status":200, "message":"Thành công", "data":{...}}
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Thành công", data);
    }

    /**
     * Tạo response thành công (200 OK) với message tùy chỉnh + data.
     *
     * Ví dụ: ApiResponse.success("Lấy danh sách sản phẩm thành công", products)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * Tạo response tạo mới thành công (201 Created) với data.
     *
     * Ví dụ: ApiResponse.created(newProduct)
     * → {"status":201, "message":"Tạo mới thành công", "data":{...}}
     */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "Tạo mới thành công", data);
    }

    /**
     * Tạo response tạo mới thành công (201 Created) với message tùy chỉnh.
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }

    /**
     * Tạo response lỗi — data luôn null.
     *
     * Dùng wildcard <?> vì khi lỗi, data = null → kiểu data không quan trọng.
     *
     * Ví dụ: ApiResponse.error(404, "Sản phẩm không tồn tại")
     * → {"status":404, "message":"Sản phẩm không tồn tại", "data":null}
     */
    public static ApiResponse<?> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    /**
     * 📘 BÀI 6 — Tạo response lỗi kèm data chi tiết.
     *
     * Dùng cho validation errors: data chứa Map<String, String> mô tả lỗi từng field.
     *
     * Ví dụ: ApiResponse.error(400, "Dữ liệu không hợp lệ", fieldErrors)
     * → {"status":400, "message":"...", "data":{"name":"Tên không được rỗng","price":"Giá phải > 0"}}
     */
    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

    // ===== Getters — Jackson cần để serialize Object → JSON =====

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
