package com.example.springbootlearning.service;

import com.example.springbootlearning.model.Product;
import com.example.springbootlearning.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 📘 BÀI 4 — Product Service (Tầng Business Logic)
 *
 * Chứa toàn bộ logic nghiệp vụ cho Product:
 * - Validation đầu vào (tên, giá, category)
 * - Kiểm tra trùng lặp
 * - Xử lý PATCH (cập nhật một phần)
 * - Logging
 *
 * Service KHÔNG biết HTTP (không có @GetMapping, @PostMapping...)
 * Service KHÔNG truy cập data trực tiếp — ủy thác cho Repository.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    // Constructor Injection — Spring tự inject ProductRepository
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        log.info("✅ ProductService đã được tạo — ProductRepository được inject thành công!");
    }

    // ===== READ Operations =====

    /**
     * Lấy tất cả sản phẩm.
     */
    public List<Product> getAllProducts() {
        log.debug("Lấy danh sách tất cả products");
        return productRepository.findAll();
    }

    /**
     * Lấy sản phẩm theo ID.
     * Sử dụng Optional.orElseThrow() — nếu không tìm thấy sẽ throw exception.
     */
    public Product getProductById(Long id) {
        log.debug("Tìm product với id={}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy product với id={}", id);
                    return new RuntimeException("Product không tồn tại với id: " + id);
                });
    }

    /**
     * Tìm kiếm sản phẩm theo keyword trong tên.
     */
    public List<Product> searchByName(String keyword) {
        log.debug("Tìm kiếm product với keyword='{}'", keyword);
        return productRepository.searchByName(keyword);
    }

    /**
     * Lọc sản phẩm theo category.
     */
    public List<Product> getByCategory(String category) {
        log.debug("Lọc product theo category='{}'", category);
        return productRepository.findByCategory(category);
    }

    /**
     * Lọc sản phẩm theo khoảng giá.
     */
    public List<Product> getByPriceRange(Double minPrice, Double maxPrice) {
        log.debug("Lọc product theo giá [{} - {}]", minPrice, maxPrice);
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    // ===== CREATE Operation =====

    /**
     * Tạo sản phẩm mới.
     * Business rules:
     *   1. Tên không được trống
     *   2. Giá phải > 0
     *   3. Tên không được trùng
     */
    public Product createProduct(Product product) {
        // Validation
        validateProduct(product);

        // Kiểm tra trùng tên
        if (productRepository.existsByName(product.getName())) {
            throw new IllegalArgumentException("Sản phẩm đã tồn tại với tên: " + product.getName());
        }

        Product saved = productRepository.save(product);
        log.info("✅ Đã tạo product mới: {}", saved);
        return saved;
    }

    // ===== UPDATE Operations =====

    /**
     * PUT — Cập nhật TOÀN BỘ sản phẩm (replace hoàn toàn).
     * Tất cả field phải được gửi, field không gửi sẽ thành null.
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        log.debug("PUT update product id={}", id);

        // Kiểm tra product tồn tại
        getProductById(id); // Throw exception nếu không tìm thấy

        // Validation dữ liệu mới
        validateProduct(updatedProduct);

        return productRepository.update(id, updatedProduct)
                .orElseThrow(() -> new RuntimeException("Không thể cập nhật product id: " + id));
    }

    /**
     * PATCH — Cập nhật MỘT PHẦN sản phẩm.
     * Chỉ field nào client gửi (not null) mới được cập nhật.
     * Các field không gửi → giữ nguyên giá trị cũ.
     */
    public Product patchProduct(Long id, Product partialUpdate) {
        log.debug("PATCH update product id={}", id);

        // Lấy product hiện tại
        Product existing = getProductById(id);

        // Chỉ cập nhật field nào KHÔNG null trong request
        if (partialUpdate.getName() != null) {
            existing.setName(partialUpdate.getName());
        }
        if (partialUpdate.getDescription() != null) {
            existing.setDescription(partialUpdate.getDescription());
        }
        if (partialUpdate.getPrice() != null) {
            if (partialUpdate.getPrice() <= 0) {
                throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
            }
            existing.setPrice(partialUpdate.getPrice());
        }
        if (partialUpdate.getCategory() != null) {
            existing.setCategory(partialUpdate.getCategory());
        }
        if (partialUpdate.getStock() != null) {
            if (partialUpdate.getStock() < 0) {
                throw new IllegalArgumentException("Số lượng tồn kho không được âm");
            }
            existing.setStock(partialUpdate.getStock());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        log.info("✅ Đã PATCH product id={}: {}", id, existing);
        return existing;
    }

    // ===== DELETE Operation =====

    /**
     * Xóa sản phẩm theo ID.
     */
    public void deleteProduct(Long id) {
        // Kiểm tra tồn tại trước khi xóa
        getProductById(id);

        boolean deleted = productRepository.deleteById(id);
        if (deleted) {
            log.info("🗑️ Đã xóa product id={}", id);
        }
    }

    // ===== Private Helpers =====

    /**
     * Validate dữ liệu product — tách thành method riêng để tái sử dụng.
     */
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        if (product.getPrice() == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Danh mục sản phẩm không được để trống");
        }
    }
}
