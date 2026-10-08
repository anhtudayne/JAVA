package com.example.springbootlearning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 📘 BÀI 6 — Request DTO cho PUT /api/v1/products/{id} (Với Validation)
 *
 * Dùng cho cập nhật TOÀN BỘ sản phẩm (PUT).
 * Validation rules giống ProductCreateRequest vì PUT yêu cầu gửi đầy đủ fields.
 *
 * ❌ KHÔNG có: id, createdAt → Server quản lý
 *
 * 📘 BÀI 8 — @Schema: mô tả + example cho Swagger UI.
 */
@Schema(description = "Dữ liệu cập nhật TOÀN BỘ sản phẩm — body của PUT /api/v1/products/{id}")
public class ProductUpdateRequest {

    @Schema(description = "Tên sản phẩm mới", example = "MacBook Pro M4 Max")
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 200, message = "Tên sản phẩm phải từ 2 đến 200 ký tự")
    private String name;

    @Schema(description = "Mô tả chi tiết (không bắt buộc)", example = "Bản nâng cấp RAM 64GB")
    private String description;

    @Schema(description = "Giá sản phẩm (USD), phải > 0", example = "2999.0")
    @NotNull(message = "Giá sản phẩm không được để trống")
    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private Double price;

    @Schema(description = "Danh mục sản phẩm", example = "Laptop")
    @NotBlank(message = "Danh mục sản phẩm không được để trống")
    private String category;

    @Schema(description = "Số lượng tồn kho (>= 0)", example = "20")
    @PositiveOrZero(message = "Số lượng tồn kho không được âm")
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
