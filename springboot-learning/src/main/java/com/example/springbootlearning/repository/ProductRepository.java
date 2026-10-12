package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 📘 BÀI 10 & 11 — Product Repository (Spring Data JPA, Advanced Query Methods & Specifications)
 *
 * Mở rộng:
 *   - Derived Query Methods (có hỗ trợ Pageable).
 *   - Custom JPQL Queries (@Query).
 *   - Native SQL Queries (nativeQuery = true).
 *   - Bulk Update Queries (@Modifying).
 *   - JOIN FETCH nạp Tags của Product.
 *   - JpaSpecificationExecutor: Dynamic queries an toàn với Criteria API và phân trang.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // ===== 1. DERIVED QUERY METHODS =====

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // ===== 2. CUSTOM JPQL QUERIES =====

    /**
     * Tìm sản phẩm theo category, sắp xếp theo giá giảm dần.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category) ORDER BY p.price DESC")
    List<Product> findByCategorySortedByPriceDesc(@Param("category") String category);

    /**
     * Tìm kiếm đa điều kiện: Tên HOẶC Mô tả chứa từ khóa.
     */
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Nạp Product kèm tập hợp Tags thông qua JOIN FETCH (chống N+1).
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Product> findByIdWithTags(@Param("id") Long id);

    /**
     * Thống kê số lượng sản phẩm và giá trung bình theo từng danh mục.
     */
    @Query("SELECT p.category, COUNT(p), AVG(p.price) FROM Product p GROUP BY p.category")
    List<Object[]> getCategoryStatistics();

    // ===== 3. NATIVE SQL QUERIES =====

    /**
     * Native SQL: Tìm sản phẩm sắp hết hàng (tồn kho dưới ngưỡng).
     */
    @Query(value = "SELECT * FROM products WHERE stock < :threshold ORDER BY stock ASC", nativeQuery = true)
    List<Product> findLowStockProductsNative(@Param("threshold") int threshold);

    // ===== 4. BULK OPERATIONS VỚI @Modifying =====

    /**
     * Giảm tồn kho nguyên tử (Atomic Update) trực tiếp bằng SQL.
     * clearAutomatically = true giúp xóa First-level cache để tránh đọc lại dữ liệu cũ.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int reduceStockAtomic(@Param("productId") Long productId, @Param("quantity") int quantity);
}
