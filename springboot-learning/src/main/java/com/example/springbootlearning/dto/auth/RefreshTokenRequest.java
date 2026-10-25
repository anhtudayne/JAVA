package com.example.springbootlearning.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 📘 BÀI 19 — DTO yêu cầu làm mới Access Token từ Refresh Token
 */
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
