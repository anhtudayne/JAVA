package com.example.springbootlearning.exception;

/**
 * 📘 BÀI 16 — Ngoại lệ khi gọi External API gặp lỗi
 */
public class ExternalApiException extends BusinessException {

    public ExternalApiException(int statusCode, String message) {
        super(statusCode, "EXTERNAL_API_ERROR", message);
    }
}
