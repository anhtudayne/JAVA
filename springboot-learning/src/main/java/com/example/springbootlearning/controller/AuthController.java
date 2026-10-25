package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.auth.AuthResponse;
import com.example.springbootlearning.dto.auth.JwtAuthResponse;
import com.example.springbootlearning.dto.auth.LoginRequest;
import com.example.springbootlearning.dto.auth.RefreshTokenRequest;
import com.example.springbootlearning.dto.auth.RegisterRequest;
import com.example.springbootlearning.exception.DuplicateResourceException;
import com.example.springbootlearning.model.Role;
import com.example.springbootlearning.model.User;
import com.example.springbootlearning.repository.RoleRepository;
import com.example.springbootlearning.repository.UserRepository;
import com.example.springbootlearning.security.CustomUserDetails;
import com.example.springbootlearning.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 📘 BÀI 18 & 19 — AuthController (JWT Authentication & RBAC)
 *
 * Cung cấp các API:
 * - POST /api/auth/register : Đăng ký tài khoản mới & phát hành cặp JWT (Access + Refresh Token)
 * - POST /api/auth/login    : Xác thực credentials qua AuthenticationManager & cấp cặp JWT Token
 * - POST /api/auth/refresh  : Cấp Access Token mới từ Refresh Token hợp lệ
 * - GET  /api/auth/me       : Lấy thông tin cá nhân từ SecurityContext qua @AuthenticationPrincipal
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * API Đăng ký tài khoản — Trả về JWT Token để Client sử dụng ngay lập tức
     */
    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Tìm role ROLE_USER hoặc khởi tạo nếu chưa có
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        User user = new User(
                null,
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                true
        );
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);

        // Sinh cặp JWT Access Token và Refresh Token
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Set<String> roles = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        JwtAuthResponse response = new JwtAuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration() / 1000,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                roles,
                "Đăng ký tài khoản thành công!"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API Đăng nhập — Xác thực credentials & cấp cặp JWT Token
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Thực hiện xác thực thông qua AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Sinh cặp JWT Access Token và Refresh Token
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        JwtAuthResponse response = new JwtAuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration() / 1000,
                userDetails.getId(),
                userDetails.getFullName(),
                userDetails.getEmail(),
                roles,
                "Đăng nhập thành công!"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * API Làm mới Access Token từ Refresh Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            String email = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token không hợp lệ hoặc đã hết hạn!");
            }

            // Sinh Access Token mới
            String newAccessToken = jwtService.generateAccessToken(userDetails);

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            Long id = null;
            String fullName = null;
            if (userDetails instanceof CustomUserDetails customUser) {
                id = customUser.getId();
                fullName = customUser.getFullName();
            }

            JwtAuthResponse response = new JwtAuthResponse(
                    newAccessToken,
                    refreshToken, // Giữ nguyên refresh token
                    "Bearer",
                    jwtService.getAccessTokenExpiration() / 1000,
                    id,
                    fullName,
                    userDetails.getUsername(),
                    roles,
                    "Làm mới token thành công!"
            );

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token không hợp lệ hoặc đã hết hạn: " + ex.getMessage());
        }
    }

    /**
     * API Lấy thông tin người dùng đang đăng nhập từ SecurityContext.
     * Hỗ trợ đồng thời cả:
     * - Bài 19: CustomUserDetails từ JwtAuthenticationFilter
     * - Bài 20: Jwt từ OAuth2 Resource Server (NimbusJwtDecoder)
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa xác thực người dùng");
        }

        Long id = null;
        String fullName = null;
        String email = null;
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Kịch bản Bài 19: Principal là CustomUserDetails
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            id = userDetails.getId();
            fullName = userDetails.getFullName();
            email = userDetails.getEmail();
        }
        // Kịch bản Bài 20: Principal là Jwt (OAuth2 Resource Server)
        else if (authentication.getPrincipal() instanceof Jwt jwt) {
            email = jwt.getSubject();
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim instanceof Number number) {
                id = number.longValue();
            }
            fullName = jwt.getClaimAsString("fullName");
            if (fullName == null) {
                fullName = email;
            }
        } else {
            email = authentication.getName();
            fullName = email;
        }

        AuthResponse response = new AuthResponse(
                id,
                fullName,
                email,
                roles,
                "Lấy thông tin người dùng thành công"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 📘 BÀI 20: Endpoint kiểm tra toàn bộ Claims của Access Token được giải mã bởi JwtDecoder.
     */
    @GetMapping("/token-claims")
    public ResponseEntity<Map<String, Object>> getTokenClaims(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa xác thực");
        }

        Map<String, Object> result = new LinkedHashMap<>();

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            result.put("mode", "Bài 20 — OAuth2 Resource Server (NimbusJwtDecoder)");
            result.put("subject", jwt.getSubject());
            result.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : "Self-Issued");
            result.put("issuedAt", jwt.getIssuedAt());
            result.put("expiresAt", jwt.getExpiresAt());
            result.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            result.put("claims", jwt.getClaims());
        } else {
            result.put("mode", "Bài 19 — Custom JwtAuthenticationFilter");
            result.put("subject", authentication.getName());
            result.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            result.put("hint", "Kích hoạt profile 'oauth2' để chuyển sang dùng OAuth2 Resource Server");
        }

        return ResponseEntity.ok(result);
    }
}
