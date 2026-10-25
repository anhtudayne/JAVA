package com.example.springbootlearning.security;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 📘 BÀI 18 & 19 — Security Configuration (Spring Security 6.x+ Lambda DSL)
 *
 * Cấu hình toàn diện:
 * - Chuỗi SecurityFilterChain xử lý HTTP request pipeline
 * - Phân quyền route: public endpoints, admin endpoints, authenticated endpoints
 * - Tắt CSRF (REST API Stateless không dùng session/cookie)
 * - SessionCreationPolicy.STATELESS (không lưu session server-side, dùng JWT)
 * - Gắn JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter
 * - Cho phép CORS từ frontend dev servers
 * - Hỗ trợ H2 Console qua cấu hình frameOptions
 * - Bật Method Security (@PreAuthorize, @PostAuthorize, @Secured)
 * - Tích hợp Custom EntryPoint & AccessDeniedHandler
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Profile("!oauth2")
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(CustomAuthenticationEntryPoint authenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          JwtAuthenticationFilter jwtAuthFilter) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Cấu hình CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Tắt CSRF cho REST API
                .csrf(csrf -> csrf.disable())

                // 3. Cho phép H2-Console hiển thị trong iframe cùng origin
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 4. Phân quyền Endpoint
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints: Đăng ký, Đăng nhập, Làm mới Token
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public endpoints: Tài liệu API Swagger & OpenAPI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // H2 Database Console
                        .requestMatchers("/h2-console/**").permitAll()

                        // Cho phép khách vãng lai xem danh mục sản phẩm (GET)
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/products/**").permitAll()

                        // Các API quản trị cho phép cả ADMIN và MANAGER qua cửa FilterChain
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")

                        // Tất cả các request còn lại đều cần xác thực
                        .anyRequest().authenticated()
                )

                // 5. 📘 BÀI 19: Stateless Session Management (Không lưu session phía server)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 6. 📘 BÀI 19: Đặt JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 7. Xử lý Exception bảo mật trả về chuẩn JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .build();
    }

    /**
     * PasswordEncoder chuẩn công nghiệp: BCrypt với cost factor mặc định 10.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager để xử lý xác thực thủ công trong Login API.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Cấu hình CORS cho ứng dụng Web Client.
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
