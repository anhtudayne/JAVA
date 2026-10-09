package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 📘 BÀI 9 — Product Repository (Spring Data JPA)
 *
 * Kế thừa JpaRepository<Product, Long>:
 *   - Tham số 1: Kiểu Entity (Product)
 *   - Tham số 2: Kiểu dữ liệu của Primary Key (Long)
 *
 * 🌟 Sức mạnh của Spring Data JPA:
 *   - Bạn CHỈ KHAI BÁO interface, KHÔNG CẦN viết class triển khai!
 *   - Tự động có sẵn toàn bộ các thao tác CRUD:
 *       + save(entity)
 *       + findById(id)
 *       + findAll()
 *       + deleteById(id)
 *       + count(), existsById(id)...
 *   - Tự động sinh truy vấn SQL dựa vào tên phương thức (Derived Query Methods)!
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Tìm sản phẩm theo danh mục (không phân biệt hoa thường).
     * SQL tương đương: SELECT * FROM products WHERE UPPER(category) = UPPER(?);
     */
    List<Product> findByCategoryIgnoreCase(String category);

    /**
     * Tìm kiếm sản phẩm theo từ khoá trong tên (LIKE %keyword%, không phân biệt hoa thường).
     * SQL tương đương: SELECT * FROM products WHERE UPPER(name) LIKE UPPER('%' || ? || '%');
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /**
     * Tìm kiếm sản phẩm trong khoảng giá (inclusive).
     * SQL tương đương: SELECT * FROM products WHERE price BETWEEN ? AND ?;
     */
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    /**
     * Kiểm tra tên sản phẩm đã tồn tại trong database hay chưa (dùng khi CREATE).
     * SQL tương đương: SELECT COUNT(*) > 0 FROM products WHERE UPPER(name) = UPPER(?);
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Kiểm tra tên sản phẩm đã tồn tại ở sản phẩm KHÁC hay chưa (dùng khi UPDATE).
     * Tránh trường hợp trùng tên với sản phẩm khác nhưng vẫn cho phép giữ nguyên tên của chính nó.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
