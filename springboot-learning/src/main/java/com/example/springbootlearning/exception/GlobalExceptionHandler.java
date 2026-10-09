package com.example.springbootlearning.exception;

import com.example.springbootlearning.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 📘 BÀI 7 — Global Exception Handler (Refactored)
 *
 * Trạm bắt lỗi TẬP TRUNG cho TOÀN BỘ ứng dụng.
 * Khi bất kỳ Controller nào ném exception, Spring sẽ chuyển về đây xử lý.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *   → Mỗi @ExceptionHandler method return Object → Jackson serialize ra JSON tự động
 *
 * Exception Specificity Rule:
 *   Spring chọn handler CỤ THỂ NHẤT theo class hierarchy.
 *   ResourceNotFoundException > BusinessException > RuntimeException > Exception
 *
 * Thay đổi so với Bài 6:
 *   ✅ Thêm handler cho ResourceNotFoundException (404)
 *   ✅ Thêm handler cho DuplicateResourceException (409)
 *   ✅ Thêm handler cho HttpMessageNotReadableException (400 — JSON sai format)
 *   ✅ Xóa handler cho RuntimeException chung (quá rộng, nuốt mất lỗi thật)
 *   ✅ Xóa handler cho IllegalArgumentException (đã thay bằng Custom Exception)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ================================================================
    // ① ResourceNotFoundException → 404 Not Found
    // ================================================================
    //
    // Khi Service ném: throw new ResourceNotFoundException("Product", "id", 999)
    // → Bắt ở đây, trả 404 với message rõ ràng
    //
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("⚠️ Resource not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404
                .body(ApiResponse.error(ex.getStatusCode(), ex.getMessage()));
    }

    // ================================================================
    // ② DuplicateResourceException → 409 Conflict
    // ================================================================
    //
    // Khi Service ném: throw new DuplicateResourceException("Product", "name", "MacBook")
    // → Bắt ở đây, trả 409 (resource đã tồn tại, xung đột)
    //
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("⚠️ Duplicate resource: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(ApiResponse.error(ex.getStatusCode(), ex.getMessage()));
    }

    // ================================================================
    // ③ MethodArgumentNotValidException → 400 Bad Request (từ Bài 6)
    // ================================================================
    //
    // Khi @Valid phát hiện field vi phạm → Spring ném MethodArgumentNotValidException.
    // Trích xuất lỗi từng field thành Map<fieldName, errorMessage>.
    //
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("⚠️ Validation failed: {}", fieldErrors);

        return ResponseEntity
                .badRequest() // 400
                .body(ApiResponse.error(400, "Dữ liệu không hợp lệ", fieldErrors));
    }

    // ================================================================
    // ④ HttpMessageNotReadableException → 400 Bad Request (JSON sai format)
    // ================================================================
    //
    // Xảy ra khi:
    //   - Client gửi JSON sai cú pháp: { "name": "abc }  (thiếu dấu ")
    //   - Client gửi body rỗng (Content-Type: application/json nhưng body trống)
    //   - Kiểu dữ liệu không khớp: { "price": "abc" }  (String thay vì Number)
    //
    // Jackson không thể deserialize → ném HttpMessageNotReadableException
    // TRƯỚC KHI đến Controller → cần bắt riêng để trả message thân thiện
    //
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("⚠️ Malformed JSON request: {}", ex.getMessage());

        return ResponseEntity
                .badRequest() // 400
                .body(ApiResponse.error(400, "Request body không đúng định dạng JSON"));
    }

    // ================================================================
    // ⑤ Exception — Catch-all (LUÔN ĐẶT CUỐI CÙNG!)
    // ================================================================
    // 🩺 BÀI 9: Bắt lỗi khi không tìm thấy Static Resource (ví dụ favicon.ico)
    // Trả về 404 gọn gàng, tránh bị Exception.class bắt và log error 500
    // ================================================================
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.trace("🔍 Static resource không tồn tại: {}", ex.getResourcePath());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "Tài nguyên tĩnh không tồn tại: " + ex.getResourcePath()));
    }

    // ================================================================
    //
    // Bắt MỌI exception không mong đợi: NullPointerException, lỗi Database, lỗi IO...
    //
    // ⚠️ QUAN TRỌNG:
    //   - KHÔNG trả ex.getMessage() cho Client → có thể lộ thông tin nội bộ
    //   - Luôn trả message chung chung cho Client
    //   - log.error() với exception object → ghi FULL STACK TRACE vào log file
    //
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllUncaughtException(Exception ex) {
        log.error("❌ Unexpected error: ", ex); // ex ở cuối → SLF4J in full stack trace

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .body(ApiResponse.error(500, "Đã có lỗi xảy ra, vui lòng thử lại sau"));
    }
}
