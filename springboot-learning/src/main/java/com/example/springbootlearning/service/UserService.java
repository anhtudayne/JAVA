package com.example.springbootlearning.service;

import com.example.springbootlearning.model.User;
import com.example.springbootlearning.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 📘 BÀI 2 — Service Layer (Tầng xử lý business logic)
 *
 * @Service đánh dấu class này là Spring Bean thuộc tầng Business Logic.
 *
 * Vai trò của @Service:
 * 1. Báo cho Spring biết: "Đây là bean chứa logic nghiệp vụ" → đưa vào IoC Container
 * 2. Tầng Service KHÔNG biết HTTP (không có @GetMapping, @PostMapping...)
 * 3. Tầng Service KHÔNG trực tiếp truy cập database — nó ủy thác cho Repository
 *
 * Kiến trúc 3 tầng (Layered Architecture):
 *   Controller → Service → Repository
 *   (HTTP)       (Logic)   (Data)
 *
 * Tại sao cần Service layer?
 * - Tách biệt business logic khỏi Controller (Controller chỉ lo HTTP)
 * - Tách biệt business logic khỏi Repository (Repository chỉ lo data access)
 * - Một Service có thể được gọi từ nhiều Controller
 * - Dễ test: mock Repository khi test Service (sẽ học ở Bài 14)
 *
 * 🔑 DEPENDENCY INJECTION — TRỌNG TÂM BÀI 2:
 * UserService cần UserRepository để hoạt động
 * → UserRepository là "dependency" của UserService
 * → Spring tự động "inject" (tiêm) UserRepository vào qua Constructor
 */
@Service
public class UserService {

    // SLF4J Logger — cách log chuẩn trong Spring Boot
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // ========================================
    // 🔑 DEPENDENCY INJECTION qua Constructor
    // ========================================
    //
    // UserRepository là dependency của UserService.
    // Khai báo là "private final":
    //   - private: chỉ class này truy cập được
    //   - final: không thể thay đổi sau khi khởi tạo (immutable)
    //            → đảm bảo dependency luôn ổn định
    private final UserRepository userRepository;

    /**
     * 🔑 CONSTRUCTOR INJECTION — Best practice được Spring team khuyến nghị
     *
     * Khi Spring Boot khởi động:
     * 1. Tạo bean UserRepository (vì có @Repository)
     * 2. Tạo bean UserService (vì có @Service)
     * 3. Thấy constructor cần UserRepository → tự inject bean đã tạo ở bước 1
     *
     * Lưu ý: Từ Spring 4.3+, nếu class chỉ có 1 constructor
     *         thì KHÔNG CẦN ghi @Autowired — Spring tự hiểu.
     *         (Class này chỉ có 1 constructor → không cần @Autowired)
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("✅ UserService đã được tạo — UserRepository được inject thành công!");
    }

    // ===== Business Methods =====

    public List<User> getAllUsers() {
        log.debug("Lấy danh sách tất cả users");
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        log.debug("Tìm user với id={}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy user với id={}", id);
                    return new RuntimeException("User không tồn tại với id: " + id);
                });
    }

    public User createUser(User user) {
        // Business rule: validate dữ liệu
        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }

        // Business rule: kiểm tra email trùng lặp
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + user.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("Đã tạo user mới: {}", savedUser);
        return savedUser;
    }

    public void deleteUser(Long id) {
        // Kiểm tra user có tồn tại không trước khi xóa
        getUserById(id); // Sẽ throw exception nếu không tìm thấy
        boolean deleted = userRepository.deleteById(id);
        if (deleted) {
            log.info("Đã xóa user với id={}", id);
        }
    }
}
