package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.Product;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 📘 BÀI 4 — Product Repository (Tầng Data Access)
 *
 * Quản lý dữ liệu Product trong bộ nhớ (in-memory ArrayList).
 * Từ Bài 9 trở đi, lớp này sẽ được thay thế bằng JPA Repository kết nối database thật.
 *
 * @Repository cho Spring biết đây là Bean thuộc tầng Data Access.
 */
@Repository
public class ProductRepository {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    // Seed data — tạo sẵn dữ liệu mẫu khi ứng dụng khởi động
    public ProductRepository() {
        save(new Product(null, "MacBook Pro M3", "Laptop cao cấp của Apple", 2499.99, "Laptop", 25));
        save(new Product(null, "iPhone 16 Pro", "Điện thoại flagship Apple", 1199.99, "Smartphone", 100));
        save(new Product(null, "Samsung Galaxy S25", "Flagship Android Samsung", 999.99, "Smartphone", 80));
        save(new Product(null, "Dell XPS 15", "Laptop mỏng nhẹ cao cấp", 1799.99, "Laptop", 30));
        save(new Product(null, "AirPods Pro 3", "Tai nghe chống ồn Apple", 249.99, "Phụ kiện", 200));
        save(new Product(null, "Bàn phím Keychron K8", "Bàn phím cơ wireless", 89.99, "Phụ kiện", 150));
    }

    // ===== CRUD Operations =====

    public List<Product> findAll() {
        return Collections.unmodifiableList(products);
    }

    public Optional<Product> findById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Product save(Product product) {
        product.setId(idCounter.incrementAndGet());
        product.setCreatedAt(java.time.LocalDateTime.now());
        product.setUpdatedAt(java.time.LocalDateTime.now());
        products.add(product);
        return product;
    }

    /**
     * Cập nhật product — tìm index → thay thế element tại vị trí đó.
     * Trả về Optional chứa product đã cập nhật, hoặc Optional.empty() nếu không tìm thấy.
     */
    public Optional<Product> update(Long id, Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(id)) {
                updatedProduct.setId(id);
                updatedProduct.setCreatedAt(products.get(i).getCreatedAt()); // Giữ nguyên ngày tạo
                updatedProduct.setUpdatedAt(java.time.LocalDateTime.now());
                products.set(i, updatedProduct);
                return Optional.of(updatedProduct);
            }
        }
        return Optional.empty();
    }

    public boolean deleteById(Long id) {
        return products.removeIf(p -> p.getId().equals(id));
    }

    // ===== Query Methods =====

    /**
     * Tìm kiếm theo category.
     */
    public List<Product> findByCategory(String category) {
        return products.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm theo keyword trong tên sản phẩm (case-insensitive).
     */
    public List<Product> searchByName(String keyword) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Tìm sản phẩm trong khoảng giá.
     */
    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        return products.stream()
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra tên sản phẩm đã tồn tại chưa.
     */
    public boolean existsByName(String name) {
        return products.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
    }
}
