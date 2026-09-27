package com.example.springbootlearning.runner;

import com.example.springbootlearning.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 📘 BÀI 2 — CommandLineRunner cập nhật
 *
 * Bản thân StartupRunner cũng minh họa DI:
 * Nó inject UserService để in thông tin users khi startup!
 */
@Component
public class StartupRunner implements CommandLineRunner {

    private final UserService userService;

    // Constructor Injection — inject UserService
    public StartupRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║       🚀 Spring Boot Learning - Bài 2: IoC & DI         ║");
        System.out.println("║       ✅ Ứng dụng đã khởi động thành công!              ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  API endpoints:                                          ║");
        System.out.println("║                                                          ║");
        System.out.println("║  GET    http://localhost:8080/api/users                   ║");
        System.out.println("║  GET    http://localhost:8080/api/users/{id}              ║");
        System.out.println("║  POST   http://localhost:8080/api/users                   ║");
        System.out.println("║  DELETE  http://localhost:8080/api/users/{id}              ║");
        System.out.println("║                                                          ║");
        System.out.println("║  (Bài 1) GET  http://localhost:8080/hello                ║");
        System.out.println("║  (Bài 1) GET  http://localhost:8080/info                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Minh họa DI: StartupRunner inject UserService → gọi method
        System.out.println("📋 Users hiện có trong hệ thống (seed data):");
        userService.getAllUsers().forEach(user ->
                System.out.println("   " + user)
        );
        System.out.println();
    }
}
