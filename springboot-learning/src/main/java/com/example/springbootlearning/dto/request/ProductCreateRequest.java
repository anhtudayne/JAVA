package com.example.springbootlearning.dto.request;

/**
 * 📘 BÀI 5 — Request DTO cho POST /api/v1/products
 *
 * Chỉ chứa những field mà CLIENT ĐƯỢC PHÉP gửi khi tạo sản phẩm mới.
 *
 * ❌ KHÔNG có: id, createdAt, updatedAt → Server tự sinh
 *
 * 🔑 Tại sao cần class riêng thay vì dùng Product entity?
 *   1. Bảo mật: Client không thể ghi đè id, createdAt
 *   2. Validation riêng: Bài 6 sẽ thêm @NotBlank, @Min... trên từng field
 *   3. Tách biệt: Thay đổi Entity không ảnh hưởng API contract
 *
 * Jackson cần:
 *   - No-args constructor (để tạo object)
 *   - Setter methods (để gán giá trị từ JSON)
 */
public class ProductCreateRequest {

    private String name;
    private String description;
    private Double price;
    private String category;
    private Integer stock;

    // ===== Constructor mặc định — BẮT BUỘC cho Jackson =====
    public ProductCreateRequest() {
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
        return "ProductCreateRequest{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                '}';
    }
}
