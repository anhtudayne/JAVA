package com.example.springbootlearning.service;

import com.example.springbootlearning.dto.request.ProductCreateRequest;
import com.example.springbootlearning.dto.request.ProductUpdateRequest;
import com.example.springbootlearning.dto.response.ProductResponse;
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
 * 📘 BÀI 5 — Product Service (Refactored với DTO Pattern)
 *
 * Thay đổi so với Bài 4:
 *   ① Nhận Request DTO (ProductCreateRequest, ProductUpdateRequest) thay vì Entity
 *   ② Trả Response DTO (ProductResponse) thay vì Entity
 *   ③ Chuyển đổi DTO ↔ Entity bên trong Service
 *
 * Luồng xử lý:
 *   Controller → Service (nhận DTO) → chuyển sang Entity → Repository → Entity → chuyển sang DTO → Controller
 *
 * Service KHÔNG biết HTTP (không có ResponseEntity, @GetMapping...)
 * Service làm việc với cả DTO (giao tiếp với Controller) và Entity (giao tiếp với Repository)
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
     *
     * Stream API pipeline:
     *   products.stream()                     → Mở stream
     *   .map(ProductResponse::fromEntity)     → Chuyển mỗi Product → ProductResponse
     *   .collect(Collectors.toList())         → Thu thập kết quả vào List
     *
     * ProductResponse::fromEntity là Method Reference — viết gọn của:
     *   .map(product -> ProductResponse.fromEntity(product))
     */
    public List<ProductResponse> getAllProducts() {
        log.debug("Lấy danh sách tất cả products");
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm theo ID → trả ProductResponse.
     * Throw RuntimeException nếu không tìm thấy (Bài 7 sẽ thay bằng custom exception).
     */
    public ProductResponse getProductById(Long id) {
        log.debug("Tìm product với id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy product với id={}", id);
                    return new RuntimeException("Product không tồn tại với id: " + id);
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
     * Luồng chuyển đổi:
     *   ① ProductCreateRequest (DTO) → Product (Entity)     [copy field]
     *   ② Product (Entity) → productRepository.save()       [lưu vào storage]
     *   ③ Product (Entity) → ProductResponse (DTO)          [fromEntity]
     */
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("📥 Tạo product mới từ request: {}", request);

        // 📘 BÀI 6: Không cần validateCreateRequest() nữa!
        // @Valid trong Controller đã tự động kiểm tra trước khi request vào được đến đây.
        // Nếu code chạy được tới dòng này → dữ liệu CHẮC CHẮN hợp lệ rồi.
        // Kiểm tra trùng tên
        if (productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Sản phẩm đã tồn tại với tên: " + request.getName());
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
        log.info("✅ Đã tạo product mới: {}", saved);

        // ③ Chuyển Entity → Response DTO
        return ProductResponse.fromEntity(saved);
    }

    // ================================================================
    // UPDATE Operations — Nhận DTO, trả DTO
    // ================================================================

    /**
     * PUT — Cập nhật TOÀN BỘ sản phẩm.
     *
     * Nhận ProductUpdateRequest (DTO) thay vì Product (Entity).
     * → Client KHÔNG thể ghi đè id, createdAt.
     */
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.debug("PUT update product id={}", id);

        // Kiểm tra product tồn tại
        productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product không tồn tại với id: " + id));

        // 📘 BÀI 6: Không cần validateUpdateRequest() nữa — @Valid đã kiểm tra rồi!
        // Chuyển DTO → Entity mới
        Product updatedProduct = new Product();
        updatedProduct.setName(request.getName());
        updatedProduct.setDescription(request.getDescription());
        updatedProduct.setPrice(request.getPrice());
        updatedProduct.setCategory(request.getCategory());
        updatedProduct.setStock(request.getStock() != null ? request.getStock() : 0);

        Product saved = productRepository.update(id, updatedProduct)
                .orElseThrow(() -> new RuntimeException("Không thể cập nhật product id: " + id));

        log.info("✅ Đã PUT update product id={}: {}", id, saved);
        return ProductResponse.fromEntity(saved);
    }

    /**
     * PATCH — Cập nhật MỘT PHẦN sản phẩm.
     *
     * Dùng Map<String, Object> để nhận chỉ những field client muốn cập nhật.
     * Field nào không có trong Map → giữ nguyên giá trị cũ.
     */
    public ProductResponse patchProduct(Long id, Map<String, Object> updates) {
        log.debug("PATCH update product id={}", id);

        // Lấy product hiện tại
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product không tồn tại với id: " + id));

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
        log.info("✅ Đã PATCH product id={}: {}", id, existing);
        return ProductResponse.fromEntity(existing);
    }

    // ================================================================
    // DELETE Operation
    // ================================================================

    /**
     * Xóa sản phẩm theo ID.
     * Throw RuntimeException nếu không tìm thấy.
     */
    public void deleteProduct(Long id) {
        // Kiểm tra tồn tại trước khi xóa
        productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product không tồn tại với id: " + id));

        boolean deleted = productRepository.deleteById(id);
        if (deleted) {
            log.info("🗑️ Đã xóa product id={}", id);
        }
    }

    // 📘 BÀI 6: Đã xóa validateCreateRequest() và validateUpdateRequest()
    // Thay thế bằng Jakarta Bean Validation annotations trên DTO + @Valid trong Controller
}
