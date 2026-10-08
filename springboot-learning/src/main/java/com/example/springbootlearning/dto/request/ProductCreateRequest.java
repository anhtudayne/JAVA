package com.example.springbootlearning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
 *
 * 📘 BÀI 8 — @Schema: thêm mô tả + giá trị mẫu cho Swagger UI.
 *   springdoc TỰ ĐỌC @NotBlank/@Size/@Positive → required/minLength/minimum,
 *   nên @Schema chỉ cần bổ sung phần NGỮ NGHĨA (description, example).
 */
@Schema(description = "Dữ liệu tạo sản phẩm mới — body của POST /api/v1/products")
public class ProductCreateRequest {

    @Schema(description = "Tên sản phẩm (duy nhất trong hệ thống)", example = "MacBook Pro M4 16 inch")
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 200, message = "Tên sản phẩm phải từ 2 đến 200 ký tự")
    private String name;

    // description không bắt buộc → không cần annotation validation
    @Schema(description = "Mô tả chi tiết (không bắt buộc)", example = "Chip M4 Pro, RAM 32GB, SSD 1TB")
    private String description;

    @Schema(description = "Giá sản phẩm (USD), phải > 0", example = "2499.99")
    @NotNull(message = "Giá sản phẩm không được để trống")
    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private Double price;

    @Schema(description = "Danh mục sản phẩm", example = "Laptop")
    @NotBlank(message = "Danh mục sản phẩm không được để trống")
    private String category;

    @Schema(description = "Số lượng tồn kho (>= 0)", example = "15")
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
