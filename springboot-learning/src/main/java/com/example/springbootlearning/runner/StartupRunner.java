package com.example.springbootlearning.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 📘 BÀI 1 - CommandLineRunner: Chạy code khi ứng dụng vừa khởi động xong
 *
 * CommandLineRunner là một interface đặc biệt của Spring Boot.
 * Khi Spring Boot hoàn tất khởi động, nó sẽ tự động gọi method run()
 * của tất cả bean implement CommandLineRunner.
 *
 * Sử dụng thực tế:
 * - Seed dữ liệu ban đầu vào database
 * - Kiểm tra kết nối database/external service
 * - Log thông tin cấu hình khi startup
 * - Chạy migration scripts
 *
 * @Component đánh dấu class này là một Spring Bean
 * → Spring sẽ tự tạo instance và quản lý lifecycle
 */
@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║       🚀 Spring Boot Learning - Bài 1                   ║");
        System.out.println("║       ✅ Ứng dụng đã khởi động thành công!              ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  Thử truy cập các API sau:                              ║");
        System.out.println("║                                                          ║");
        System.out.println("║  GET  http://localhost:8080/hello                         ║");
        System.out.println("║  GET  http://localhost:8080/info                          ║");
        System.out.println("║  GET  http://localhost:8080/greet?name=TenCuaBan          ║");
        System.out.println("║  GET  http://localhost:8080/hello/TenCuaBan               ║");
        System.out.println("║                                                          ║");
        System.out.println("║  📝 Bạn có thể dùng Postman hoặc curl để test           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        // In thông tin môi trường
        System.out.println("📋 Thông tin môi trường:");
        System.out.println("   Java Version : " + System.getProperty("java.version"));
        System.out.println("   Java Vendor  : " + System.getProperty("java.vendor"));
        System.out.println("   OS           : " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("   User Dir     : " + System.getProperty("user.dir"));
        System.out.println();
    }
}
