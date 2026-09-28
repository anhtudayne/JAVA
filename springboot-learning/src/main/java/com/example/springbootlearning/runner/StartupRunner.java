package com.example.springbootlearning.runner;

import com.example.springbootlearning.service.ProductService;
import com.example.springbootlearning.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 📘 BÀI 4 — CommandLineRunner cập nhật
 *
 * Bản thân StartupRunner cũng minh họa DI:
 * Nó inject UserService và ProductService để in thông tin khi startup!
 */
@Component
public class StartupRunner implements CommandLineRunner {

    private final UserService userService;
    private final ProductService productService;

    // Constructor Injection — inject cả 2 service
    public StartupRunner(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║       🚀 Spring Boot Learning — Ứng dụng đã khởi động thành công!   ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  (Bài 2) User API:                                                   ║");
        System.out.println("║    GET    /api/users           POST   /api/users                      ║");
        System.out.println("║    GET    /api/users/{id}      DELETE /api/users/{id}                  ║");
        System.out.println("║                                                                       ║");
        System.out.println("║  (Bài 4) Product CRUD API:                                            ║");
        System.out.println("║    GET    /api/v1/products                 (filter: ?category=&minPrice=&maxPrice=) ║");
        System.out.println("║    GET    /api/v1/products/{id}            (@PathVariable)             ║");
        System.out.println("║    GET    /api/v1/products/search?keyword= (@RequestParam)             ║");
        System.out.println("║    POST   /api/v1/products                 (@RequestBody JSON)         ║");
        System.out.println("║    PUT    /api/v1/products/{id}            (Replace toàn bộ)           ║");
        System.out.println("║    PATCH  /api/v1/products/{id}            (Cập nhật một phần)         ║");
        System.out.println("║    DELETE /api/v1/products/{id}                                        ║");
        System.out.println("║    GET    /api/v1/products/debug/headers   (@RequestHeader)            ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Minh họa DI: Hiển thị seed data
        System.out.println("📋 Users hiện có (Bài 2):");
        userService.getAllUsers().forEach(user ->
                System.out.println("   " + user)
        );

        System.out.println();
        System.out.println("📦 Products hiện có (Bài 4):");
        productService.getAllProducts().forEach(product ->
                System.out.println("   " + product)
        );
        System.out.println();
    }
}
