package com.example.springbootlearning.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * 📘 BÀI 18 — Role Entity (Spring Security Authorization)
 *
 * Đại diện cho vai trò/quyền hạn trong hệ thống:
 * - ROLE_USER: Người dùng thông thường
 * - ROLE_ADMIN: Quản trị viên hệ thống
 * - ROLE_MANAGER: Quản lý nghiệp vụ
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", name='" + name + "'}";
    }
}
