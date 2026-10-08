package com.example.springbootlearning.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 📘 BÀI 8 — Cấu hình OpenAPI / Swagger UI
 *
 * Tùy chỉnh thông tin hiển thị trên Swagger UI:
 *  ① Info: tiêu đề, phiên bản, mô tả, contact, license
 *  ② Security Scheme "Bearer JWT" → hiện nút 🔒 Authorize (chuẩn bị cho Bài 18-20)
 *  ③ GroupedOpenApi: chia API thành nhóm (dropdown "Select a definition" góc phải trên)
 *
 * Truy cập:
 *  - Swagger UI : http://localhost:8080/swagger-ui.html
 *  - JSON spec  : http://localhost:8080/v3/api-docs
 *  - YAML spec  : http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer JWT";

    // Tái sử dụng giá trị từ application.properties (Bài 3: @Value)
    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🚀 Spring Boot Learning API")
                        .version(appVersion)
                        .description("""
                                RESTful API cho dự án **Spring Boot Learning** — từ cơ bản đến nâng cao.

                                **Chức năng hiện tại:**
                                - ✅ CRUD quản lý sản phẩm (Product)
                                - ✅ Validation dữ liệu đầu vào (Jakarta Bean Validation)
                                - ✅ Xử lý lỗi tập trung (Global Exception Handling)
                                - ✅ Logging với TraceId (SLF4J + MDC)

                                **Quy ước Response:** mọi response đều bọc trong `ApiResponse<T>`
                                `{ status, message, data, timestamp }` — lỗi trả về `data: null` hoặc map lỗi từng field.
                                """)
                        .contact(new Contact()
                                .name("Spring Boot Learning Team")
                                .email("dev@springbootlearning.com")
                                .url("https://github.com/springboot-learning"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("📚 Spring Boot Reference Documentation")
                        .url("https://docs.spring.io/spring-boot/"))
                // ② Security Scheme — hiện tại CHƯA có Spring Security nên nút Authorize chỉ mang tính minh họa
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập JWT token (không cần prefix 'Bearer ')")));
    }

    // ③ Nhóm API — mỗi bean GroupedOpenApi = 1 "definition" trong dropdown Swagger UI
    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("1-product-management")
                .displayName("📦 Product Management (v1)")
                .pathsToMatch("/api/v1/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("9-all")
                .displayName("🌐 Tất cả API")
                .pathsToMatch("/**")
                .build();
    }
}
