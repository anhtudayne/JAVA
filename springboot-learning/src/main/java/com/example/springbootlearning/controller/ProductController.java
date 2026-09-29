package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.request.ProductCreateRequest;
import com.example.springbootlearning.dto.request.ProductUpdateRequest;
import com.example.springbootlearning.dto.response.ApiResponse;
import com.example.springbootlearning.dto.response.ProductResponse;
import com.example.springbootlearning.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * 📘 BÀI 5 — Product REST Controller (Refactored)
 *
 * Thay đổi so với Bài 4:
 *   ① Dùng ResponseEntity<T> để kiểm soát HTTP Status Code
 *   ② Dùng Request DTO (ProductCreateRequest, ProductUpdateRequest) thay vì Entity
 *   ③ Dùng Response DTO (ProductResponse record) thay vì Entity
 *   ④ Dùng ApiResponse<T> wrapper cho response thống nhất
 *
 * Status Code Convention:
 *   - GET thành công  → 200 OK
 *   - POST thành công → 201 Created + Location header
 *   - PUT thành công  → 200 OK
 *   - DELETE thành công → 204 No Content (không có body)
 *   - Lỗi client      → 400, 404, 409...
 *
 * Luồng: Client → DispatcherServlet → Controller (DTO) → Service (DTO↔Entity) → Repository (Entity)
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    // Constructor Injection
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ================================================================
    // ① GET — Lấy tất cả sản phẩm (có hỗ trợ filter)
    // ================================================================
    //
    // GET /api/v1/products                            → Lấy tất cả
    // GET /api/v1/products?category=Laptop            → Lọc theo category
    // GET /api/v1/products?minPrice=500&maxPrice=2000  → Lọc theo khoảng giá
    //
    // Trả về: ResponseEntity<ApiResponse<List<ProductResponse>>>
    //   - ResponseEntity: kiểm soát status code (200)
    //   - ApiResponse: wrapper thống nhất {status, message, data, timestamp}
    //   - List<ProductResponse>: danh sách DTO (không phải Entity)
    //
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        log.info("📥 GET /api/v1/products — category={}, minPrice={}, maxPrice={}",
                category, minPrice, maxPrice);

        List<ProductResponse> products;

        if (category != null && !category.isBlank()) {
            products = productService.getByCategory(category);
        } else if (minPrice != null && maxPrice != null) {
            products = productService.getByPriceRange(minPrice, maxPrice);
        } else {
            products = productService.getAllProducts();
        }

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách sản phẩm thành công", products)
        );
    }

    // ================================================================
    // ② GET — Lấy sản phẩm theo ID
    // ================================================================
    //
    // GET /api/v1/products/42
    //
    // Trả về: 200 OK + ApiResponse<ProductResponse>
    // Lỗi:   RuntimeException → Spring mặc định trả 500
    //         (Bài 7 sẽ thêm @ExceptionHandler để trả 404)
    //
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        log.info("📥 GET /api/v1/products/{} — Tìm product theo ID", id);

        ProductResponse product = productService.getProductById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy sản phẩm thành công", product)
        );
    }

    // ================================================================
    // ③ GET — Tìm kiếm sản phẩm theo tên
    // ================================================================
    //
    // GET /api/v1/products/search?keyword=macbook
    //
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam String keyword
    ) {
        log.info("📥 GET /api/v1/products/search — keyword='{}'", keyword);

        List<ProductResponse> products = productService.searchByName(keyword);

        return ResponseEntity.ok(
                ApiResponse.success("Tìm kiếm sản phẩm thành công", products)
        );
    }

    // ================================================================
    // ④ POST — Tạo sản phẩm mới
    // ================================================================
    //
    // POST /api/v1/products
    // Content-Type: application/json
    // Body: {"name":"iPad Pro M4","description":"Tablet","price":1299,"category":"Tablet","stock":25}
    //
    // Thay đổi Bài 5:
    //   ✅ Nhận ProductCreateRequest (DTO) thay vì Product (Entity)
    //      → Client KHÔNG thể gửi id, createdAt, updatedAt
    //   ✅ Trả 201 Created thay vì 200 OK
    //   ✅ Set Location header chỉ URL của resource vừa tạo
    //
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request  // 📘 BÀI 6: @Valid kích hoạt validation tự động
    ) {
        log.info("📥 POST /api/v1/products — Tạo product mới: {}", request.getName());

        ProductResponse created = productService.createProduct(request);

        // URI — địa chỉ resource vừa tạo (trả trong Location header)
        URI location = URI.create("/api/v1/products/" + created.id());
        // Lưu ý: created.id() — Record getter KHÔNG có prefix "get"

        return ResponseEntity
                .created(location)  // 201 Created + Location header
                .body(ApiResponse.created("Tạo sản phẩm thành công", created));
    }

    // ================================================================
    // ⑤ PUT — Cập nhật TOÀN BỘ sản phẩm
    // ================================================================
    //
    // PUT /api/v1/products/42
    // Body: {"name":"MacBook Pro M4","description":"Updated","price":2999,"category":"Laptop","stock":20}
    //
    // Nhận ProductUpdateRequest (DTO) → Client không thể ghi đè id, createdAt
    //
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request  // 📘 BÀI 6: @Valid kích hoạt validation tự động
    ) {
        log.info("📥 PUT /api/v1/products/{} — Cập nhật toàn bộ product", id);

        ProductResponse updated = productService.updateProduct(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật sản phẩm thành công", updated)
        );
    }

    // ================================================================
    // ⑥ PATCH — Cập nhật MỘT PHẦN sản phẩm
    // ================================================================
    //
    // PATCH /api/v1/products/42
    // Body: {"price": 1999}   → Chỉ cập nhật giá
    //
    // Dùng Map<String, Object> thay vì DTO — vì PATCH chỉ gửi field cần thay đổi,
    // không biết trước field nào sẽ có → Map linh hoạt hơn.
    //
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> patchProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        log.info("📥 PATCH /api/v1/products/{} — Cập nhật một phần product", id);

        ProductResponse patched = productService.patchProduct(id, updates);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật sản phẩm thành công", patched)
        );
    }

    // ================================================================
    // ⑦ DELETE — Xóa sản phẩm
    // ================================================================
    //
    // DELETE /api/v1/products/42
    //
    // Trả về: 204 No Content (KHÔNG có body)
    // Đây là chuẩn REST cho DELETE thành công:
    //   - Resource đã bị xóa → không còn gì để trả
    //   - ResponseEntity<Void> nghĩa là "body rỗng"
    //
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("📥 DELETE /api/v1/products/{}", id);

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();  // 204 No Content
    }

    // ================================================================
    // ⑧ GET — Demo @RequestHeader (giữ nguyên từ Bài 4)
    // ================================================================
    @GetMapping("/debug/headers")
    public ResponseEntity<ApiResponse<Map<String, String>>> debugHeaders(
            @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            @RequestHeader(value = "X-Custom-Header", required = false) String customHeader
    ) {
        log.info("📥 GET /api/v1/products/debug/headers — Reading HTTP headers");

        Map<String, String> headers = Map.of(
                "User-Agent", userAgent,
                "Accept-Language", language,
                "X-Custom-Header", customHeader != null ? customHeader : "Không có"
        );

        return ResponseEntity.ok(
                ApiResponse.success("Đọc HTTP headers thành công", headers)
        );
    }
}
