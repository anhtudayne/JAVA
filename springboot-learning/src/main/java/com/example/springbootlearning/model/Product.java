package com.example.springbootlearning.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 📘 BÀI 9 — Product Entity (JPA & Hibernate ORM)
 *
 * Chuyển đổi từ POJO thuần sang JPA Entity được ánh xạ với bảng `products` trong Database.
 *
 * Các annotation chính của JPA/Hibernate:
 *   - @Entity: Đánh dấu class này là 1 Entity được quản lý bởi JPA/Hibernate.
 *   - @Table(name = "products"): Chỉ định tên bảng trong cơ sở dữ liệu.
 *   - @Id: Khóa chính (Primary Key).
 *   - @GeneratedValue(strategy = GenerationType.IDENTITY): Khóa chính tự tăng (Auto-increment).
 *   - @Column: Tùy biến cột (tên cột, độ dài, ràng buộc nullable, unique, updatable).
 *   - @CreationTimestamp: Tự gán thời điểm tạo khi INSERT (Hibernate tự động).
 *   - @UpdateTimestamp: Tự gán thời điểm sửa khi UPDATE (Hibernate tự động).
 *
 * ⚠️ Bắt buộc theo đặc tả JPA:
 *   1. Phải có Constructor không tham số (No-args constructor) để Hibernate khởi tạo qua Reflection.
 *   2. Class không được là `final` để Hibernate có thể tạo CGLIB/ByteBuddy Proxy cho Lazy Loading.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== Constructors =====

    /**
     * BẮT BUỘC cho JPA/Hibernate (Reflection) và Jackson.
     */
    public Product() {
    }

    /**
     * Constructor thuận tiện cho việc tạo entity mới trước khi lưu.
     */
    public Product(Long id, String name, String description, Double price,
                   String category, Integer stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                '}';
    }
}
