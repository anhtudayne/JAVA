package com.example.springbootlearning.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 📘 BÀI 1 - Controller đầu tiên
 *
 * @RestController = @Controller + @ResponseBody
 * - @Controller: Đánh dấu class này là một Spring MVC Controller (xử lý HTTP request)
 * - @ResponseBody: Tự động serialize return value thành JSON (nhờ thư viện Jackson)
 *
 * Khi Spring Boot khởi động, nó sẽ:
 * 1. Quét package com.example.springbootlearning và tất cả sub-packages
 * 2. Tìm thấy @RestController trên class này
 * 3. Đăng ký nó như một Spring Bean trong ApplicationContext
 * 4. Map các @GetMapping URL vào các method tương ứng
 */
@RestController
public class HelloController {

    // ==========================================
    // API 1: Trả về chuỗi đơn giản
    // ==========================================

    /**
     * @GetMapping("/hello") — Map HTTP GET request tới URL /hello vào method này
     *
     * Khi client gửi: GET http://localhost:8080/hello
     * → Spring Boot gọi method sayHello()
     * → Return chuỗi String, nhờ @ResponseBody sẽ gửi thẳng về client
     */
    @GetMapping("/hello")
    public String sayHello() {
        return "Xin chào! Đây là API đầu tiên của tôi với Spring Boot!";
    }

    // ==========================================
    // API 2: Trả về JSON tự động
    // ==========================================

    /**
     * Khi return một Object (Map, List, POJO...), Spring Boot tự động:
     * 1. Sử dụng Jackson ObjectMapper để serialize thành JSON
     * 2. Set Content-Type: application/json trong HTTP Response
     *
     * Map<String, Object> sẽ được chuyển thành JSON object:
     * {
     *   "app": "Spring Boot Learning",
     *   "version": "1.0",
     *   "author": "Sinh viên IT năm 4",
     *   "timestamp": "2024-01-01T12:00:00"
     * }
     */
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("app", "Spring Boot Learning");
        info.put("version", "1.0");
        info.put("author", "Sinh viên IT năm 4");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("springBootVersion", "4.0.8");
        info.put("timestamp", LocalDateTime.now().toString());
        return info; // Jackson tự chuyển Map → JSON
    }

    // ==========================================
    // API 3: Nhận tham số từ URL (@RequestParam)
    // ==========================================

    /**
     * @RequestParam — Lấy giá trị từ query string
     *
     * Ví dụ: GET /greet?name=Nguyen
     * → name = "Nguyen"
     *
     * defaultValue: giá trị mặc định nếu client không truyền tham số
     * Ví dụ: GET /greet → name = "World" (dùng defaultValue)
     */
    @GetMapping("/greet")
    public Map<String, String> greet(
            @RequestParam(defaultValue = "World") String name) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xin chào, " + name + "!");
        response.put("tip", "Thử thay đổi ?name=TenCuaBan trên URL");
        return response;
    }

    // ==========================================
    // API 4: Nhận tham số từ đường dẫn (@PathVariable)
    // ==========================================

    /**
     * @PathVariable — Lấy giá trị từ path URL
     *
     * Ví dụ: GET /hello/Nguyen
     * → {name} = "Nguyen"
     *
     * Khác với @RequestParam:
     * - @RequestParam: /greet?name=Nguyen    (query parameter)
     * - @PathVariable: /hello/Nguyen         (path parameter)
     */
    @GetMapping("/hello/{name}")
    public String sayHelloTo(@PathVariable String name) {
        return "Xin chào, " + name + "! Chào mừng bạn đến với Spring Boot!";
    }
}
