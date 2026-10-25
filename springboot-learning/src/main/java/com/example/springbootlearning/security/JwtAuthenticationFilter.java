package com.example.springbootlearning.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 📘 BÀI 19 — JwtAuthenticationFilter
 *
 * Chạy đúng 1 lần cho mỗi HTTP Request (OncePerRequestFilter):
 * 1. Chặn request, kiểm tra header "Authorization: Bearer <token>"
 * 2. Trích xuất username (email) từ JWT token
 * 3. Nếu chưa xác thực trong SecurityContext, tải UserDetails từ DB
 * 4. Xác thực tính toàn vẹn của token (chữ ký HMAC, hạn sử dụng)
 * 5. Nạp UsernamePasswordAuthenticationToken vào SecurityContextHolder
 * 6. Chuyển quyền cho filter tiếp theo trong chuỗi FilterChain
 *
 * Lưu ý: Chỉ kích hoạt khi KHÔNG chạy profile "oauth2" (profile oauth2 dùng BearerTokenAuthenticationFilter của Spring)
 */
@Component
@Profile("!oauth2")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Kiểm tra header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); // Bỏ tiền tố "Bearer "

        try {
            // 2. Trích xuất email (subject)
            final String userEmail = jwtService.extractUsername(jwt);

            // 3. Nếu có email và chưa được xác thực trong SecurityContext của thread hiện tại
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 4. Kiểm tra tính hợp lệ của token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Nạp thông tin xác thực vào SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT hợp lệ cho người dùng: {}", userEmail);
                }
            }
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token đã hết hạn: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.warn("JWT token không hợp lệ: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Lỗi khi xử lý xác thực JWT: {}", ex.getMessage());
        }

        // 6. Chuyển tiếp request cho filter tiếp theo
        filterChain.doFilter(request, response);
    }
}
