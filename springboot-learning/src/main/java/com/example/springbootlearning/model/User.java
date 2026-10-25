package com.example.springbootlearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 📘 BÀI 10 & 18 — User Entity (JPA ORM & Spring Security)
 *
 * Mối quan hệ:
 *   - @OneToMany với Order (1 User có nhiều Order) -> INVERSE SIDE (mappedBy = "user").
 *   - @ManyToMany với Role (1 User có nhiều Role) -> OWNING SIDE (thông qua bảng user_roles).
 *   - Áp dụng CascadeType.ALL, orphanRemoval = true cho orders.
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
     * 📘 BÀI 18: Password đã được hash một chiều bằng BCrypt.
     * Sử dụng @JsonIgnore để không bao giờ serialize password ra JSON response.
     */
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Trạng thái hoạt động của tài khoản (true = hoạt động, false = bị khóa)
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * 📘 BÀI 18: Danh sách vai trò của người dùng trong hệ thống (ROLE_USER, ROLE_ADMIN, ...)
     */
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @BatchSize(size = 20)
    private Set<Role> roles = new HashSet<>();

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

    public User(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.active = true;
    }

    public User(Long id, String name, String email, String password, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.active = active;
    }

    // ===== Convenience Helper Methods =====

    public void addOrder(Order order) {
        this.orders.add(order);
        order.setUser(this);
    }

    public void removeOrder(Order order) {
        this.orders.remove(order);
        order.setUser(null);
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', active=" + active + "}";
    }
}
