package com.example.springbootlearning.specification;

import com.example.springbootlearning.model.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * 📘 BÀI 11 — UserSpecification (Dynamic Query Builder cho User Entity)
 */
public class UserSpecification {

    /**
     * Lọc theo tên người dùng (chứa từ khóa, case-insensitive).
     */
    public static Specification<User> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase().trim() + "%");
        };
    }

    /**
     * Lọc theo email người dùng (chứa từ khóa, case-insensitive).
     */
    public static Specification<User> emailContains(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase().trim() + "%");
        };
    }

    /**
     * Tổng hợp các tiêu chí tìm kiếm động cho User.
     */
    public static Specification<User> buildFilter(String name, String email) {
        return Specification.where(nameContains(name))
                .and(emailContains(email));
    }
}
