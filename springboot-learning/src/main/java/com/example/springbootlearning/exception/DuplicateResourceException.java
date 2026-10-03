package com.example.springbootlearning.exception;

/**
 * 📘 BÀI 7 — Ném khi cố tạo resource mà đã tồn tại (vi phạm unique constraint).
 *
 * HTTP Status: 409 Conflict
 * Error Code:  DUPLICATE_RESOURCE
 *
 * Ví dụ sử dụng:
 *   throw new DuplicateResourceException("Product", "name", "MacBook");
 *   → message: "Product đã tồn tại với name: MacBook"
 *
 * HTTP 409 Conflict — Resource bị xung đột với trạng thái hiện tại của hệ thống.
 * Ý nghĩa: "Yêu cầu của bạn không thể thực hiện vì XỬ LÝ XUNG ĐỘT với dữ liệu hiện có."
 */
public class DuplicateResourceException extends BusinessException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                409,
                "DUPLICATE_RESOURCE",
                String.format("%s đã tồn tại với %s: %s", resourceName, fieldName, fieldValue)
        );
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // ===== Getters =====

    public String getResourceName() { return resourceName; }
    public String getFieldName()    { return fieldName; }
    public Object getFieldValue()   { return fieldValue; }
}
