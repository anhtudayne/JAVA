package com.example.springbootlearning.security;

import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

/**
 * 📘 BÀI 20 — OAuth2 Resource Server Security Configuration
 *
 * Kích hoạt khi profile "oauth2" được bật (SPRING_PROFILES_ACTIVE=oauth2 hoặc --spring.profiles.active=oauth2).
 *
 * Điểm khác biệt so với Bài 19:
 * 1. Không dùng JwtAuthenticationFilter tự viết nữa, thay bằng BearerTokenAuthenticationFilter chuẩn của Spring.
 * 2. Xác thực và giải mã JWT thông qua JwtDecoder (NimbusJwtDecoder).
 * 3. Chuyển đổi claims thành GrantedAuthority qua JwtRoleAuthenticationConverter.
 * 4. Xử lý chuẩn OAuth2 Bearer Token Authentication Exception với RFC 6750 headers.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Profile("oauth2")
public class OAuth2SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtRoleAuthenticationConverter jwtRoleAuthenticationConverter;

    @Value("${jwt.secret}")
    private String secretKey;

    public OAuth2SecurityConfig(CustomAuthenticationEntryPoint authenticationEntryPoint,
                                CustomAccessDeniedHandler accessDeniedHandler,
                                JwtRoleAuthenticationConverter jwtRoleAuthenticationConverter) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.jwtRoleAuthenticationConverter = jwtRoleAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. CORS Configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Disable CSRF (Stateless REST API)
                .csrf(csrf -> csrf.disable())

                // 3. Cho phép hiển thị H2 Console trong iframe cùng origin
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 4. Phân quyền Endpoint
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger / OpenAPI documentation
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // H2 Database Console
                        .requestMatchers("/h2-console/**").permitAll()

                        // Public product APIs (GET)
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/products/**").permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")

                        // Mọi request còn lại bắt buộc xác thực
                        .anyRequest().authenticated()
                )

                // 5. Stateless Session Management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 6. 📘 BÀI 20: Tích hợp OAuth2 Resource Server với JWT Decoder & Converter chuẩn Spring
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtRoleAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 7. Exception Handling chuẩn JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .build();
    }

    /**
     * 📘 BÀI 20: JwtDecoder Bean cho Self-Issued JWT (khóa đối xứng HMAC-SHA256).
     *
     * Khi kết nối với OAuth2 Authorization Server thực tế (Keycloak, Auth0, Okta, Google):
     * Chỉ cần cấu hình spring.security.oauth2.resourceserver.jwt.issuer-uri,
     * Spring Boot sẽ tự động tạo NimbusJwtDecoder với public key từ endpoint .well-known/openid-configuration.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
        decoder.setJwtValidator(JwtValidators.createDefault());
        return decoder;
    }

    /**
     * PasswordEncoder chuẩn công nghiệp: BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager để xử lý xác thực credentials (cho các API /login, /register).
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Cấu hình CORS cho Web Client.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
