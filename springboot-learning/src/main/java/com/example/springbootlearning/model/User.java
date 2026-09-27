package com.example.springbootlearning.model;

/**
 * 📘 BÀI 2 — Model (POJO - Plain Old Java Object)
 *
 * Model là class đại diện cho dữ liệu trong ứng dụng.
 * Nó KHÔNG có annotation Spring nào — chỉ là Java thuần.
 *
 * POJO = class Java bình thường, không kế thừa framework class nào.
 * Ở các bài sau (Bài 9), class này sẽ trở thành JPA Entity khi kết nối database.
 */
public class User {

    private Long id;
    private String name;
    private String email;

    // ===== Constructors =====

    // Constructor mặc định (cần cho Jackson deserialize JSON → Object)
    public User() {
    }

    // Constructor đầy đủ
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // ===== Getters & Setters =====
    // Jackson cần getter để serialize Object → JSON
    // Jackson cần setter (hoặc constructor) để deserialize JSON → Object

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

    // toString — hữu ích khi debug/log
    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
