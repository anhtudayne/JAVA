package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 📘 BÀI 15: Slice Testing ProductRepository với @DataJpaTest
 *
 * Kiểm tra các query method tự sinh (Derived Query Methods) và Custom JPQL:
 *   - findByCategoryIgnoreCase
 *   - findByNameContainingIgnoreCase
 *   - findByPriceBetween
 */
@DataJpaTest
@DisplayName("ProductRepository - Slice Test với @DataJpaTest")
class ProductRepositoryDataJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Lọc sản phẩm theo Category có phân trang")
    void testFindByCategoryIgnoreCase_WithPagination() {
        // Given
        Product p1 = new Product(null, "MacBook Air M2", "Laptop Apple", 1099.0, "Laptop", 15);
        Product p2 = new Product(null, "Dell XPS 13", "Laptop Dell", 1299.0, "Laptop", 10);
        Product p3 = new Product(null, "iPhone 15", "Smartphone", 799.0, "Phone", 30);

        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.persist(p3);
        entityManager.flush();
        entityManager.clear();

        // When
        Page<Product> laptops = productRepository.findByCategoryIgnoreCase("laptop", PageRequest.of(0, 10));

        // Then
        assertThat(laptops.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(laptops.getContent()).extracting(Product::getName)
                .contains("MacBook Air M2", "Dell XPS 13");
    }

    @Test
    @DisplayName("Tìm kiếm sản phẩm theo khoảng giá findByPriceBetween")
    void testFindByPriceBetween() {
        // Given
        Product budget = new Product(null, "Chuột không dây", "Phụ kiện", 25.0, "Accessory", 50);
        Product mid = new Product(null, "Bàn phím cơ", "Phụ kiện", 85.0, "Accessory", 20);
        Product high = new Product(null, "Màn hình 4K", "Màn hình", 450.0, "Monitor", 5);

        entityManager.persist(budget);
        entityManager.persist(mid);
        entityManager.persist(high);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Product> products = productRepository.findByPriceBetween(20.0, 100.0);

        // Then
        assertThat(products).extracting(Product::getName)
                .contains("Chuột không dây", "Bàn phím cơ")
                .doesNotContain("Màn hình 4K");
    }

    @Test
    @DisplayName("Tìm kiếm sản phẩm theo từ khóa chứa trong tên findByNameContainingIgnoreCase")
    void testFindByNameContainingIgnoreCase() {
        // Given
        Product prod = new Product(null, "Sony WH-1000XM5", "Tai nghe chống ồn", 399.0, "Audio", 12);
        entityManager.persistAndFlush(prod);
        entityManager.clear();

        // When
        List<Product> results = productRepository.findByNameContainingIgnoreCase("wh-1000");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo("Sony WH-1000XM5");
    }
}
