package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 📘 BÀI 2 — Repository Layer (Tầng truy cập dữ liệu)
 *
 * @Repository đánh dấu class này là Spring Bean thuộc tầng Data Access.
 *
 * Vai trò của @Repository:
 * 1. Báo cho Spring biết: "Đây là bean quản lý dữ liệu" → Spring tạo instance và đưa vào IoC Container
 * 2. Tự động dịch (translate) các exception từ tầng database thành DataAccessException của Spring
 *    (hữu ích khi dùng JPA/JDBC — sẽ học ở Bài 9)
 * 3. Giúp code rõ ràng về mặt kiến trúc — nhìn annotation biết ngay tầng nào
 *
 * Hiện tại: Dùng List trong bộ nhớ thay cho database thật.
 * Từ Bài 9: Sẽ chuyển sang JpaRepository kết nối database thật.
 */
@Repository
public class UserRepository {

    // Dữ liệu giả lập (thay cho database)
    private final List<User> users = new ArrayList<>();

    // AtomicLong để tạo ID tự tăng (thread-safe)
    private final AtomicLong idCounter = new AtomicLong(0);

    // Khởi tạo dữ liệu mẫu (seed data)
    public UserRepository() {
        save(new User(null, "Nguyễn Văn A", "a@gmail.com"));
        save(new User(null, "Trần Thị B", "b@gmail.com"));
        save(new User(null, "Lê Văn C", "c@gmail.com"));
    }

    /**
     * Lấy tất cả users
     *
     * Collections.unmodifiableList() trả về view KHÔNG thể sửa đổi,
     * bảo vệ dữ liệu nội bộ khỏi bị thay đổi từ bên ngoài.
     */
    public List<User> findAll() {
        return Collections.unmodifiableList(users);
    }

    /**
     * Tìm user theo ID
     *
     * Optional<T> — container có thể chứa hoặc không chứa giá trị.
     * Thay vì return null (dễ gây NullPointerException),
     * ta return Optional.empty() khi không tìm thấy.
     *
     * Cách dùng Optional:
     *   .isPresent()     → kiểm tra có giá trị không
     *   .get()           → lấy giá trị (⚠️ throw exception nếu empty)
     *   .orElse(default) → lấy giá trị, hoặc trả default nếu empty
     *   .orElseThrow()   → lấy giá trị, hoặc throw exception nếu empty
     *   .map(fn)         → biến đổi giá trị bên trong (nếu có)
     */
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    /**
     * Tìm user theo email
     */
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Lưu user mới (gán ID tự động)
     */
    public User save(User user) {
        user.setId(idCounter.incrementAndGet());
        users.add(user);
        return user;
    }

    /**
     * Xóa user theo ID
     * Trả về true nếu xóa thành công, false nếu không tìm thấy
     */
    public boolean deleteById(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    public boolean existsByEmail(String email) {
        return users.stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }
}
