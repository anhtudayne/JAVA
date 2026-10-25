package com.example.springbootlearning.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 📘 BÀI 19 — JwtService
 *
 * Dịch vụ xử lý toàn bộ vòng đời của JSON Web Token (JJWT 0.12.x):
 * - Tạo Access Token (chứa email, roles, userId, fullName)
 * - Tạo Refresh Token (chứa email, thời hạn dài hơn)
 * - Giải mã (parse) và xác thực (verify) chữ ký HMAC-SHA256
 * - Trích xuất claims: Subject, Expiration, Roles,...
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Tạo Access Token từ UserDetails với các Claims tùy biến
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Nhúng danh sách authorities (roles) vào payload
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        if (userDetails instanceof CustomUserDetails customUser) {
            extraClaims.put("userId", customUser.getId());
            extraClaims.put("fullName", customUser.getFullName());
        }

        return buildToken(extraClaims, userDetails.getUsername(), accessTokenExpiration);
    }

    /**
     * Tạo Refresh Token (chỉ chứa subject định danh, không chứa quyền)
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshTokenExpiration);
    }

    /**
     * Kiểm tra tính hợp lệ của Token dựa trên UserDetails và hạn sử dụng
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Trích xuất Subject (Username / Email) từ token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Trích xuất Claim cụ thể bằng cách truyền resolver Function
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Lấy thời hạn tồn tại của Access Token (ms)
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Lấy thời hạn tồn tại của Refresh Token (ms)
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    // ==================== Private Helper Methods ====================

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
