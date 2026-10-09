package com.example.springbootlearning.model;

import jakarta.persistence.*;

/**
 * 📘 BÀI 9 — User Entity (JPA & Hibernate ORM)
 *
 * Chuyển đổi từ POJO thuần (Bài 2) sang JPA Entity được ánh xạ với bảng `users` trong Database.
 * Sẵn sàng cho việc thiết lập quan hệ One-to-Many / Many-to-One ở Bài 10.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    // ===== Constructors =====

    // Bắt buộc cho Hibernate (No-args constructor)
    public User() {
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // ===== Getters & Setters =====

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
