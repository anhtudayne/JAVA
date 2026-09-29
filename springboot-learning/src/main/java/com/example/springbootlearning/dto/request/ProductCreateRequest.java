package com.example.springbootlearning.dto.request;

import jakarta.validation.constraints.*;

/**
 * 📘 BÀI 6 — Request DTO cho POST /api/v1/products (Với Validation)
 *
 * Thay đổi so với Bài 5:
 *   ✅ Gắn Jakarta Validation annotations lên từng field
 *   ✅ Spring MVC sẽ tự động kiểm tra khi Controller nhận @Valid @RequestBody
 *   ✅ Nếu vi phạm → ném MethodArgumentNotValidException (không vào Controller)
 *
 * Mỗi annotation chịu trách nhiệm 1 quy tắc duy nhất:
 *   @NotBlank  → Không rỗng, không null, không toàn khoảng trắng (chỉ String)
 *   @Size      → Giới hạn độ dài chuỗi
 *   @NotNull   → Không null (dùng cho kiểu số: Double, Integer)
 *   @Positive  → Phải > 0 (nghiêm ngặt, không cho phép 0)
 *   @PositiveOrZero → Phải >= 0
 *
 * Jackson vẫn cần:
 *   - No-args constructor (để tạo object)
 *   - Setter methods (để gán giá trị từ JSON)
 */
public class ProductCreateRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 200, message = "Tên sản phẩm phải từ 2 đến 200 ký tự")
    private String name;

    // description không bắt buộc → không cần annotation validation
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private Double price;

    @NotBlank(message = "Danh mục sản phẩm không được để trống")
    private String category;

    @PositiveOrZero(message = "Số lượng tồn kho không được âm")
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
