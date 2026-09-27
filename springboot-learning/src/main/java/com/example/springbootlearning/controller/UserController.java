package com.example.springbootlearning.controller;

import com.example.springbootlearning.model.User;
import com.example.springbootlearning.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 📘 BÀI 2 — Controller Layer (Tầng xử lý HTTP)
 *
 * @RestController = @Controller + @ResponseBody
 *   → Nhận HTTP request, gọi Service xử lý, trả về JSON response
 *
 * @RequestMapping("/api/users")
 *   → Tất cả endpoint trong class này đều bắt đầu bằng /api/users
 *
 * 🔑 DEPENDENCY INJECTION:
 * UserController cần UserService để hoạt động.
 * → Spring tự inject UserService qua constructor.
 *
 * Chuỗi DI hoàn chỉnh:
 *   Spring tạo UserRepository  (vì @Repository)
 *        ↓ inject vào
 *   Spring tạo UserService     (vì @Service)
 *        ↓ inject vào
 *   Spring tạo UserController  (vì @RestController)
 *
 * Controller KHÔNG chứa business logic — chỉ:
 * 1. Nhận request từ client
 * 2. Gọi Service để xử lý
 * 3. Trả response cho client
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection — Spring inject UserService vào đây
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ===== GET /api/users — Lấy tất cả users =====
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // ===== GET /api/users/{id} — Lấy user theo ID =====
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // ===== POST /api/users — Tạo user mới =====
    //
    // @RequestBody: Tự động deserialize JSON body → Java Object (nhờ Jackson)
    //
    // Khi client gửi:
    //   POST /api/users
    //   Content-Type: application/json
    //   Body: {"name": "Nguyễn Văn D", "email": "d@gmail.com"}
    //
    // Jackson sẽ:
    //   1. Đọc JSON string
    //   2. Tạo new User()
    //   3. Gọi setName("Nguyễn Văn D"), setEmail("d@gmail.com")
    //   4. Truyền User object vào method
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // ===== DELETE /api/users/{id} — Xóa user =====
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "Đã xóa user với id: " + id;
    }
}
