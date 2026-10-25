package com.example.springbootlearning.controller;

import com.example.springbootlearning.model.User;
import com.example.springbootlearning.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 📘 BÀI 18 — AdminController
 *
 * Minh họa kết hợp cả 2 cấp độ bảo vệ:
 * 1. URL-Level Security: Tất cả request tới /api/admin/** đều được chặn trước bởi SecurityFilterChain (hasRole('ADMIN'))
 * 2. Method-Level Security: Dùng @PreAuthorize và @Secured để kiểm tra chi tiết theo nghiệp vụ
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Dashboard quản trị viên — Chỉ dành riêng cho ADMIN
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("status", "ACTIVE");
        stats.put("message", "Chào mừng Quản trị viên truy cập trang quản trị hệ thống!");
        return ResponseEntity.ok(stats);
    }

    /**
     * Lấy toàn bộ người dùng — Minh họa @PreAuthorize
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> response = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getName());
                    map.put("email", u.getEmail());
                    map.put("active", u.isActive());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Khóa tài khoản — Minh họa @Secured
     */
    @PatchMapping("/users/{id}/toggle-status")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với id: " + id));

        user.setActive(!user.isActive());
        userRepository.save(user);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("email", user.getEmail());
        result.put("active", user.isActive());
        result.put("message", user.isActive() ? "Đã kích hoạt tài khoản" : "Đã vô hiệu hóa tài khoản");

        return ResponseEntity.ok(result);
    }
}
