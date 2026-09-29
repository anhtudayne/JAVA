package com.example.springbootlearning.dto.request;

/**
 * 📘 BÀI 5 — Request DTO cho PUT /api/v1/products/{id}
 *
 * Dùng cho cập nhật TOÀN BỘ sản phẩm (PUT).
 *
 * Cấu trúc giống ProductCreateRequest nhưng tách riêng vì:
 *   - PUT có thể có logic validation khác (ví dụ: không cho đổi category)
 *   - Bài 6 sẽ thêm validation annotation khác nhau
 *   - Tách biệt giúp mỗi endpoint có DTO riêng, dễ maintain
 *
 * ❌ KHÔNG có: id, createdAt → Server quản lý
 */
public class ProductUpdateRequest {

    private String name;
    private String description;
    private Double price;
    private String category;
    private Integer stock;

    // ===== Constructor mặc định — BẮT BUỘC cho Jackson =====
    public ProductUpdateRequest() {
    }

    // ===== Getters & Setters =====

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

    @Override
    public String toString() {
        return "ProductUpdateRequest{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                '}';
    }
}
