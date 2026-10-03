package com.example.springbootlearning.exception;

/**
 * 📘 BÀI 7 — Ném khi resource được yêu cầu KHÔNG TỒN TẠI trong hệ thống.
 *
 * HTTP Status: 404 Not Found
 * Error Code:  RESOURCE_NOT_FOUND
 *
 * Ví dụ sử dụng:
 *   throw new ResourceNotFoundException("Product", "id", 999);
 *   → message: "Product không tìm thấy với id: 999"
 *
 *   throw new ResourceNotFoundException("User", "email", "abc@gmail.com");
 *   → message: "User không tìm thấy với email: abc@gmail.com"
 *
 * Metadata bổ sung:
 *   - resourceName: Tên loại resource (Product, User, Order...)
 *   - fieldName:    Tên field dùng để tìm (id, email, code...)
 *   - fieldValue:   Giá trị tìm kiếm (999, "abc@gmail.com"...)
 */
public class ResourceNotFoundException extends BusinessException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                404,
                "RESOURCE_NOT_FOUND",
                String.format("%s không tìm thấy với %s: %s", resourceName, fieldName, fieldValue)
        );
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // ===== Getters — GlobalExceptionHandler có thể truy xuất nếu cần =====

    public String getResourceName() { return resourceName; }
    public String getFieldName()    { return fieldName; }
    public Object getFieldValue()   { return fieldValue; }
}
