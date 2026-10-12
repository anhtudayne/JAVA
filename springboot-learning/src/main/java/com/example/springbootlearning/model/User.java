package com.example.springbootlearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 * 📘 BÀI 10 — User Entity (JPA & Hibernate ORM)
 *
 * Mối quan hệ:
 *   - @OneToMany với Order (1 User có nhiều Order) -> INVERSE SIDE (mappedBy = "user").
 *   - Áp dụng CascadeType.ALL, orphanRemoval = true.
 *   - @BatchSize(size = 20) giúp chống N+1 Query hiệu quả khi load lazy collection.
 */
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * 📖 INVERSE SIDE:
     * - mappedBy = "user": Trỏ tới thuộc tính "user" của Order entity.
     * - cascade = CascadeType.ALL: Mọi thao tác lưu/xóa User đều lan truyền tới Orders.
     * - orphanRemoval = true: Khi gỡ order khỏi List, Hibernate tự sinh câu lệnh DELETE.
     * - fetch = FetchType.LAZY: Chỉ tải orders khi thực sự truy cập qua getter.
     * - @BatchSize: Tối ưu chống N+1, gom nhóm 20 users thành 1 câu IN (...) khi tải lazy.
     */
    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 20)
    private List<Order> orders = new ArrayList<>();

    // ===== Constructors =====

    // Bắt buộc cho Hibernate (No-args constructor)
    public User() {
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // ===== Convenience Helper Methods (Đồng bộ 2 chiều) =====

    public void addOrder(Order order) {
        this.orders.add(order);
        order.setUser(this);
    }

    public void removeOrder(Order order) {
        this.orders.remove(order);
        order.setUser(null);
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

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
