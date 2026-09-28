package com.example.springbootlearning.model;

import java.time.LocalDateTime;

/**
 * 📘 BÀI 4 — Product Model (POJO)
 *
 * Đại diện cho một sản phẩm trong hệ thống.
 * Dùng để minh hoạ CRUD API hoàn chỉnh với tất cả HTTP Methods.
 *
 * ✅ Phải có:
 *   - Constructor mặc định (no-args) → Jackson cần để deserialize JSON → Object
 *   - Getter methods → Jackson cần để serialize Object → JSON
 *   - Setter methods → Jackson cần để gán giá trị khi deserialize
 */
public class Product {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Constructors =====

    // Constructor mặc định — BẮT BUỘC cho Jackson
    public Product() {
    }

    // Constructor đầy đủ — dùng trong code khi tạo object
    public Product(Long id, String name, String description, Double price,
                   String category, Integer stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
