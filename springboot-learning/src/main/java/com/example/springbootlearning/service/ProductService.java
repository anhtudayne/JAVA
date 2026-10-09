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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 📘 BÀI 9 — Product Service (Refactored với Spring Data JPA & Hibernate)
 *
 * Thay đổi so với Bài 7-8:
 *   ✅ Dùng JpaRepository thay thế cho In-Memory List
 *   ✅ Áp dụng @Transactional(readOnly = true) cho các thao tác đọc để tối ưu hiệu năng
 *   ✅ Áp dụng @Transactional cho các thao tác ghi (create, update, delete)
 *   ✅ Update Entity chuẩn JPA: load Managed Entity → set fields → Hibernate Dirty Checking / save()
 *   ✅ JpaRepository.deleteById() trả về void (thay vì boolean như custom in-memory)
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    // Constructor Injection — Spring tự động inject ProductRepository proxy
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        log.info("✅ ProductService đã được tạo — Spring Data JpaRepository được inject thành công!");
    }

    // ================================================================
    // READ Operations — Trả về ProductResponse (DTO)
    // ================================================================

    /**
     * Lấy tất cả sản phẩm trong database → chuyển sang List<ProductResponse>.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.debug("Lấy danh sách tất cả products từ database");
        List<ProductResponse> products = productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
        log.info("📦 Trả về {} sản phẩm từ database", products.size());
        return products;
    }

    /**
     * Lấy sản phẩm theo ID → trả ProductResponse.
     * Ném ResourceNotFoundException (404) nếu không tồn tại.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug("Tìm product với id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tìm thấy với id={}", id);
                    return new ResourceNotFoundException("Product", "id", id);
                });
        return ProductResponse.fromEntity(product);
    }

    /**
     * Tìm kiếm sản phẩm theo keyword trong tên → trả List<ProductResponse>.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String keyword) {
        log.debug("Tìm kiếm product với keyword='{}'", keyword);
        return productRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lọc sản phẩm theo category → trả List<ProductResponse>.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(String category) {
        log.debug("Lọc product theo category='{}'", category);
        return productRepository.findByCategoryIgnoreCase(category).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lọc sản phẩm theo khoảng giá → trả List<ProductResponse>.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getByPriceRange(Double minPrice, Double maxPrice) {
        log.debug("Lọc product theo giá [{} - {}]", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ================================================================
    // CREATE Operation — Nhận ProductCreateRequest (DTO), trả ProductResponse (DTO)
    // ================================================================

    /**
     * Tạo sản phẩm mới trong database.
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("📥 Tạo product mới: name='{}', price={}", request.getName(), request.getPrice());

        // Kiểm tra trùng tên (không phân biệt hoa thường)
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("⚠️ Product name đã tồn tại: '{}'", request.getName());
            throw new DuplicateResourceException("Product", "name", request.getName());
        }

        // ① Chuyển Request DTO → Entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock() != null ? request.getStock() : 0);

        // ② Lưu Entity vào database qua JpaRepository
        Product saved = productRepository.save(product);
        log.info("✅ Product created successfully: id={}, name='{}'", saved.getId(), saved.getName());

        // ③ Chuyển Entity → Response DTO
        return ProductResponse.fromEntity(saved);
    }

    // ================================================================
    // UPDATE Operations — Nhận DTO, trả DTO
    // ================================================================

    /**
     * PUT — Cập nhật TOÀN BỘ thông tin sản phẩm.
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.info("📥 PUT update product id={}", id);

        // ① Kiểm tra product tồn tại trong DB → 404 nếu không
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tìm thấy với id={} (PUT update)", id);
                    return new ResourceNotFoundException("Product", "id", id);
                });

        // ② Kiểm tra trùng tên với sản phẩm khác (ngoại trừ chính nó)
        if (productRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            log.warn("⚠️ Tên sản phẩm '{}' đã được sử dụng bởi sản phẩm khác", request.getName());
            throw new DuplicateResourceException("Product", "name", request.getName());
        }

        // ③ Cập nhật các trường trên Managed Entity
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setStock(request.getStock() != null ? request.getStock() : 0);

        // ④ Lưu lại (trong @Transactional, Dirty Checking cũng sẽ tự động phát hiện và sinh lệnh UPDATE)
        Product saved = productRepository.save(existingProduct);

        log.info("✅ Product updated (PUT): id={}, name='{}'", id, saved.getName());
        return ProductResponse.fromEntity(saved);
    }

    /**
     * PATCH — Cập nhật MỘT PHẦN sản phẩm.
     */
    @Transactional
    public ProductResponse patchProduct(Long id, Map<String, Object> updates) {
        log.info("📥 PATCH update product id={}, fields={}", id, updates.keySet());

        // ① Lấy product hiện tại từ DB → 404 nếu không tìm thấy
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tìm thấy với id={} (PATCH update)", id);
                    return new ResourceNotFoundException("Product", "id", id);
                });

        // ② Chỉ cập nhật field CÓ TRONG map
        if (updates.containsKey("name")) {
            String name = (String) updates.get("name");
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Tên sản phẩm không được để trống");
            }
            if (productRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new DuplicateResourceException("Product", "name", name);
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

        Product saved = productRepository.save(existing);
        log.info("✅ Product patched: id={}, updated fields={}", id, updates.keySet());
        return ProductResponse.fromEntity(saved);
    }

    // ================================================================
    // DELETE Operation
    // ================================================================

    /**
     * Xóa sản phẩm theo ID.
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("📥 Delete product id={}", id);

        // Kiểm tra tồn tại trước khi xóa → 404 nếu không
        if (!productRepository.existsById(id)) {
            log.warn("⚠️ Product không tìm thấy với id={} (DELETE)", id);
            throw new ResourceNotFoundException("Product", "id", id);
        }

        productRepository.deleteById(id);
        log.info("🗑️ Product deleted: id={}", id);
    }
}
