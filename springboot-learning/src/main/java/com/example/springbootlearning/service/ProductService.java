package com.example.springbootlearning.service;

import com.example.springbootlearning.dto.request.ProductCreateRequest;
import com.example.springbootlearning.dto.request.ProductUpdateRequest;
import com.example.springbootlearning.dto.response.ProductResponse;
import com.example.springbootlearning.exception.DuplicateResourceException;
import com.example.springbootlearning.exception.ResourceNotFoundException;
import com.example.springbootlearning.model.Product;
import com.example.springbootlearning.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 📘 BÀI 7 — Product Service (Refactored với Custom Exception & Logging)
 *
 * Thay đổi so với Bài 5-6:
 *   ✅ RuntimeException → ResourceNotFoundException (404)
 *   ✅ IllegalArgumentException → DuplicateResourceException (409)
 *   ✅ Logging nhất quán: INFO cho sự kiện thành công, WARN cho cảnh báo, ERROR cho lỗi
 *
 * Exception KHÔNG cần try-catch ở đây:
 *   → Custom Exception kế thừa RuntimeException (Unchecked)
 *   → GlobalExceptionHandler (@RestControllerAdvice) bắt tập trung
 *   → Service chỉ cần throw, không cần xử lý
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

    // ================================================================
    // READ Operations — Trả về ProductResponse (DTO)
    // ================================================================

    /**
     * Lấy tất cả sản phẩm → chuyển từ List<Product> sang List<ProductResponse>.
     */
    public List<ProductResponse> getAllProducts() {
        log.debug("Lấy danh sách tất cả products");
        List<ProductResponse> products = productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        log.info("📦 Trả về {} sản phẩm", products.size());
        return products;
    }

    /**
     * Lấy sản phẩm theo ID → trả ProductResponse.
     *
     * 📘 BÀI 7: Thay RuntimeException → ResourceNotFoundException (404)
     * → GlobalExceptionHandler bắt riêng, trả HTTP 404 chính xác
     */
    public ProductResponse getProductById(Long id) {
        log.debug("Tìm product với id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tìm thấy với id={}", id);
                    return new ResourceNotFoundException("Product", "id", id);
                    //         ^^^^^^^^^^^^^^^^^^^^^^^^ Cụ thể! 404 Not Found
                });
        return ProductResponse.fromEntity(product);
    }

    /**
     * Tìm kiếm sản phẩm theo keyword trong tên → trả List<ProductResponse>.
     */
    public List<ProductResponse> searchByName(String keyword) {
        log.debug("Tìm kiếm product với keyword='{}'", keyword);
        return productRepository.searchByName(keyword).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lọc sản phẩm theo category → trả List<ProductResponse>.
     */
    public List<ProductResponse> getByCategory(String category) {
        log.debug("Lọc product theo category='{}'", category);
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lọc sản phẩm theo khoảng giá → trả List<ProductResponse>.
     */
    public List<ProductResponse> getByPriceRange(Double minPrice, Double maxPrice) {
        log.debug("Lọc product theo giá [{} - {}]", minPrice, maxPrice);
        return productRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ================================================================
    // CREATE Operation — Nhận ProductCreateRequest (DTO), trả ProductResponse (DTO)
    // ================================================================

    /**
     * Tạo sản phẩm mới.
     *
     * 📘 BÀI 7: Thay IllegalArgumentException → DuplicateResourceException (409)
     * → GlobalExceptionHandler bắt riêng, trả HTTP 409 Conflict chính xác
     */
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("📥 Tạo product mới: name='{}', price={}", request.getName(), request.getPrice());

        // 📘 BÀI 6: @Valid trong Controller đã kiểm tra format dữ liệu
        // 📘 BÀI 7: Kiểm tra trùng tên → DuplicateResourceException (409)
        if (productRepository.existsByName(request.getName())) {
            log.warn("⚠️ Product name đã tồn tại: '{}'", request.getName());
            throw new DuplicateResourceException("Product", "name", request.getName());
            //         ^^^^^^^^^^^^^^^^^^^^^^^^^^^ Cụ thể! 409 Conflict
        }

        // ① Chuyển Request DTO → Entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock() != null ? request.getStock() : 0);

        // ② Lưu Entity
        Product saved = productRepository.save(product);
        log.info("✅ Product created successfully: id={}, name='{}'", saved.getId(), saved.getName());

        // ③ Chuyển Entity → Response DTO
        return ProductResponse.fromEntity(saved);
    }

    // ================================================================
    // UPDATE Operations — Nhận DTO, trả DTO
    // ================================================================

    /**
     * PUT — Cập nhật TOÀN BỘ sản phẩm.
     *
     * 📘 BÀI 7: RuntimeException → ResourceNotFoundException (404)
     */
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.info("📥 PUT update product id={}", id);

        // Kiểm tra product tồn tại → 404 nếu không
        productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tìm thấy với id={} (PUT update)", id);
                    return new ResourceNotFoundException("Product", "id", id);
                });

        // 📘 BÀI 6: @Valid trong Controller đã kiểm tra format dữ liệu
        // Chuyển DTO → Entity mới
        Product updatedProduct = new Product();
        updatedProduct.setName(request.getName());
        updatedProduct.setDescription(request.getDescription());
        updatedProduct.setPrice(request.getPrice());
        updatedProduct.setCategory(request.getCategory());
        updatedProduct.setStock(request.getStock() != null ? request.getStock() : 0);

        Product saved = productRepository.update(id, updatedProduct)
                .orElseThrow(() -> {
                    log.error("❌ Không thể cập nhật product id={}", id);
                    return new ResourceNotFoundException("Product", "id", id);
                });

        log.info("✅ Product updated (PUT): id={}, name='{}'", id, saved.getName());
        return ProductResponse.fromEntity(saved);
    }

    /**
     * PATCH — Cập nhật MỘT PHẦN sản phẩm.
     *
     * 📘 BÀI 7: RuntimeException → ResourceNotFoundException (404)
     */
    public ProductResponse patchProduct(Long id, Map<String, Object> updates) {
        log.info("📥 PATCH update product id={}, fields={}", id, updates.keySet());

        // Lấy product hiện tại → 404 nếu không
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tìm thấy với id={} (PATCH update)", id);
                    return new ResourceNotFoundException("Product", "id", id);
                });

        // Chỉ cập nhật field CÓ TRONG map
        if (updates.containsKey("name")) {
            String name = (String) updates.get("name");
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Tên sản phẩm không được để trống");
            }
            existing.setName(name);
        }
        if (updates.containsKey("description")) {
            existing.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("price")) {
            Double price = ((Number) updates.get("price")).doubleValue();
            if (price <= 0) {
                throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
            }
            existing.setPrice(price);
        }
        if (updates.containsKey("category")) {
            String category = (String) updates.get("category");
            if (category == null || category.isBlank()) {
                throw new IllegalArgumentException("Danh mục sản phẩm không được để trống");
            }
            existing.setCategory(category);
        }
        if (updates.containsKey("stock")) {
            Integer stock = ((Number) updates.get("stock")).intValue();
            if (stock < 0) {
                throw new IllegalArgumentException("Số lượng tồn kho không được âm");
            }
            existing.setStock(stock);
        }

        existing.setUpdatedAt(LocalDateTime.now());
        log.info("✅ Product patched: id={}, updated fields={}", id, updates.keySet());
        return ProductResponse.fromEntity(existing);
    }

    // ================================================================
    // DELETE Operation
    // ================================================================

    /**
     * Xóa sản phẩm theo ID.
     *
     * 📘 BÀI 7: RuntimeException → ResourceNotFoundException (404)
     */
    public void deleteProduct(Long id) {
        log.info("📥 Delete product id={}", id);

        // Kiểm tra tồn tại trước khi xóa → 404 nếu không
        productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tìm thấy với id={} (DELETE)", id);
                    return new ResourceNotFoundException("Product", "id", id);
                });

        boolean deleted = productRepository.deleteById(id);
        if (deleted) {
            log.info("🗑️ Product deleted: id={}", id);
        }
    }
}
