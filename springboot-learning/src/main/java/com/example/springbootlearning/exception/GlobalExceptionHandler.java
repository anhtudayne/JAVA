package com.example.springbootlearning.exception;

import com.example.springbootlearning.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 📘 BÀI 6 — Global Exception Handler
 *
 * Trạm bắt lỗi tập trung cho TOÀN BỘ dự án.
 * Khi bất kỳ Controller nào ném exception, Spring sẽ đưa về đây xử lý.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *   → Cho phép return trực tiếp Object (Spring sẽ dùng Jackson serialize ra JSON)
 *
 * Các exception được xử lý:
 *   1. MethodArgumentNotValidException — Lỗi @Valid validation
 *   2. IllegalArgumentException — Lỗi nghiệp vụ đơn giản (tên trùng, v.v.)
 *   3. RuntimeException — Lỗi không tìm thấy resource
 *   4. Exception — Catch-all cho lỗi không mong đợi (luôn đặt cuối cùng!)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ================================================================
    // ① Validation Errors — @Valid thất bại
    // ================================================================
    //
    // Khi Spring kiểm tra @Valid trên @RequestBody và phát hiện field vi phạm,
    // nó ném MethodArgumentNotValidException chứa DANH SÁCH tất cả lỗi.
    //
    // Ta trích xuất lỗi từng field thành Map<fieldName, errorMessage>
    // và trả về trong ApiResponse.data để Frontend đọc được.
    //
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        // Duyệt qua từng lỗi field:
        // - getField() → tên field bị lỗi (vd: "name", "price")
        // - getDefaultMessage() → message đã khai báo trong annotation (vd: "Tên không được rỗng")
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("⚠️ Validation failed: {}", fieldErrors);

        return ResponseEntity
                .badRequest() // HTTP 400 Bad Request
                .body(ApiResponse.error(400, "Dữ liệu không hợp lệ", fieldErrors));
    }

    // ================================================================
    // ② IllegalArgumentException — Lỗi nghiệp vụ đơn giản
    // ================================================================
    //
    // Ví dụ: Tên sản phẩm đã tồn tại, input không hợp lệ về logic nghiệp vụ
    //
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("⚠️ Business rule violation: {}", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    // ================================================================
    // ③ RuntimeException — Lỗi không tìm thấy resource (tạm thời)
    // ================================================================
    //
    // Bài 7 sẽ thay thế bằng custom ResourceNotFoundException.
    // Hiện tại dùng RuntimeException chung để bắt lỗi "Product không tồn tại"
    //
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        log.warn("⚠️ Runtime error: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    // ================================================================
    // ④ Exception — Catch-all (LUÔN ĐẶT CUỐI CÙNG!)
    // ================================================================
    //
    // Bắt mọi lỗi không mong đợi (NullPointerException, lỗi Database, v.v.)
    // Trả message chung chung cho Client (KHÔNG để lộ chi tiết kỹ thuật nội bộ)
    // Log.error ghi lại full StackTrace vào file log để Dev debug
    //
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllUncaughtException(Exception ex) {
        log.error("❌ Hệ thống gặp sự cố nghiêm trọng: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .body(ApiResponse.error(500, "Đã có lỗi xảy ra, vui lòng thử lại sau"));
    }
}
