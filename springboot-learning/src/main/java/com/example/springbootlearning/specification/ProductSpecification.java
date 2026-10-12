package com.example.springbootlearning.specification;

import com.example.springbootlearning.model.Product;
import org.springframework.data.jpa.domain.Specification;

/**
 * 📘 BÀI 11 — ProductSpecification (Specification Pattern / Dynamic Query Builder)
 *
 * Cho phép xây dựng câu truy vấn động (dynamic WHERE clause) một cách an toàn loại (type-safe)
 * bằng cách kết hợp JPA Criteria API mà không cần ghép chuỗi SQL thủ công (tránh SQL Injection).
 */
public class ProductSpecification {

    /**
     * Lọc theo tên sản phẩm (không phân biệt hoa thường, tìm kiếm gần đúng).
     */
    public static Specification<Product> hasName(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase().trim() + "%");
        };
    }

    /**
     * Lọc theo danh mục sản phẩm (không phân biệt hoa thường, so sánh chính xác).
     */
    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("category")), category.toLowerCase().trim());
        };
    }

    /**
     * Lọc theo giá tối thiểu (price >= minPrice).
     */
    public static Specification<Product> priceGreaterThanOrEqualTo(Double minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    /**
     * Lọc theo giá tối đa (price <= maxPrice).
     */
    public static Specification<Product> priceLessThanOrEqualTo(Double maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    /**
     * Lọc sản phẩm còn hàng trong kho (stock > 0).
     */
    public static Specification<Product> inStockOnly(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null || !inStock) {
                return cb.conjunction();
            }
            return cb.greaterThan(root.get("stock"), 0);
        };
    }

    /**
     * Tổng hợp tất cả tiêu chí tìm kiếm động thành 1 Specification duy nhất.
     */
    public static Specification<Product> buildFilter(String keyword, String category,
                                                     Double minPrice, Double maxPrice,
                                                     Boolean inStock) {
        return Specification.where(hasName(keyword))
                .and(hasCategory(category))
                .and(priceGreaterThanOrEqualTo(minPrice))
                .and(priceLessThanOrEqualTo(maxPrice))
                .and(inStockOnly(inStock));
    }
}
