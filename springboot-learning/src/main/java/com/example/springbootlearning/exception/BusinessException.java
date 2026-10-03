package com.example.springbootlearning.exception;

/**
 * 📘 BÀI 7 — Base class cho TẤT CẢ lỗi nghiệp vụ trong ứng dụng.
 *
 * Mọi Custom Exception (NotFound, Duplicate, Forbidden...) đều kế thừa từ class này.
 *
 * Tại sao dùng abstract?
 *   → Không cho phép new BusinessException() trực tiếp — buộc phải tạo subclass cụ thể.
 *   → Mỗi subclass mang ngữ nghĩa rõ ràng (NotFound ≠ Duplicate ≠ Forbidden).
 *
 * Tại sao extends RuntimeException (Unchecked)?
 *   → Không bắt buộc try-catch — để @RestControllerAdvice bắt lỗi tập trung.
 *   → Quy ước chuẩn trong Spring Boot cho business exception.
 *
 * Metadata mang theo:
 *   - statusCode: HTTP Status Code nên trả cho Client (404, 409, 403...)
 *   - errorCode:  Mã lỗi nội bộ để Frontend mapping UI (RESOURCE_NOT_FOUND, DUPLICATE_RESOURCE...)
 */
public abstract class BusinessException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    protected BusinessException(int statusCode, String errorCode, String message) {
        super(message);          // Gọi constructor cha: RuntimeException(String message)
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    /**
     * HTTP Status Code tương ứng (404, 409, 403...).
     * GlobalExceptionHandler dùng giá trị này để set ResponseEntity.status().
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Mã lỗi nội bộ (RESOURCE_NOT_FOUND, DUPLICATE_RESOURCE...).
     * Frontend có thể dùng để hiển thị UI tương ứng (vd: trang 404, popup lỗi trùng...).
     */
    public String getErrorCode() {
        return errorCode;
    }
}
