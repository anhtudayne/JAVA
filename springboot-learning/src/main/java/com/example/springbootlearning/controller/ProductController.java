package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.request.ProductCreateRequest;
import com.example.springbootlearning.dto.request.ProductUpdateRequest;
import com.example.springbootlearning.dto.response.ApiResponse;
import com.example.springbootlearning.dto.response.ProductResponse;
import com.example.springbootlearning.service.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
// ⚠️ KHÔNG import io.swagger.v3.oas.annotations.responses.ApiResponse
//    vì trùng tên với DTO com.example...dto.response.ApiResponse của dự án!
//    → Dùng tên đầy đủ (fully-qualified) @io.swagger.v3.oas.annotations.responses.ApiResponse
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
 *
 * 📘 BÀI 8 — Thêm OpenAPI annotations:
 *   @Tag          → Nhóm Controller trên Swagger UI
 *   @Operation    → Mô tả từng endpoint (summary + description)
 *   @ApiResponses → Liệt kê các HTTP status code có thể trả về
 *   @Parameter    → Mô tả query/path params
 *   @Hidden       → Ẩn endpoint debug khỏi tài liệu
 */
@Tag(name = "📦 Product Management", description = "CRUD API quản lý sản phẩm — tạo, đọc, cập nhật, xóa, tìm kiếm")
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
    @Operation(
            summary = "Lấy danh sách sản phẩm",
            description = """
                    Lấy tất cả sản phẩm, hỗ trợ lọc (ưu tiên theo thứ tự):
                    1. `category` → lọc theo danh mục
                    2. `minPrice` + `maxPrice` → lọc theo khoảng giá (phải truyền cả hai)
                    3. Không truyền gì → trả về tất cả
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @Parameter(description = "Lọc theo danh mục", example = "Laptop")
            @RequestParam(required = false) String category,
            @Parameter(description = "Giá tối thiểu (USD)", example = "500")
            @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Giá tối đa (USD)", example = "3000")
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
    @Operation(summary = "Lấy sản phẩm theo ID", description = "Trả về chi tiết 1 sản phẩm. Ném 404 nếu không tồn tại.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Tìm thấy sản phẩm"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy sản phẩm với ID này",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @Parameter(description = "ID sản phẩm", example = "1", required = true)
            @PathVariable Long id) {
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
    @Operation(summary = "Tìm kiếm sản phẩm theo tên", description = "Tìm kiếm không phân biệt hoa thường, khớp một phần tên.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Tìm kiếm thành công (có thể trả danh sách rỗng)")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @Parameter(description = "Từ khóa tìm kiếm", example = "macbook", required = true)
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
    @Operation(
            summary = "Tạo sản phẩm mới",
            description = """
                    Tạo sản phẩm mới. Quy tắc:
                    - Tên sản phẩm phải **duy nhất** (trùng → 409)
                    - Giá phải > 0, tồn kho >= 0 (sai → 400 kèm lỗi từng field)
                    - Thành công → **201 Created** + header `Location`
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "✅ Tạo sản phẩm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ (validation) hoặc JSON sai format",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "⚠️ Tên sản phẩm đã tồn tại",
                    content = @Content)
    })
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
    @Operation(summary = "Cập nhật TOÀN BỘ sản phẩm", description = "PUT yêu cầu gửi đầy đủ các field bắt buộc.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy sản phẩm", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "ID sản phẩm cần cập nhật", example = "1", required = true)
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
    @Operation(
            summary = "Cập nhật MỘT PHẦN sản phẩm",
            description = "Chỉ gửi field cần đổi, ví dụ: `{\"price\": 1999}`. Body là Map nên Swagger không có schema cố định."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "✅ Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy sản phẩm", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> patchProduct(
            @Parameter(description = "ID sản phẩm", example = "1", required = true)
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
    @Operation(summary = "Xóa sản phẩm", description = "Xóa thành công trả về **204 No Content** (không có body).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "✅ Xóa thành công", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "❌ Không tìm thấy sản phẩm", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID sản phẩm cần xóa", example = "1", required = true)
            @PathVariable Long id) {
        log.info("📥 DELETE /api/v1/products/{}", id);

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();  // 204 No Content
    }

    // ================================================================
    // ⑧ GET — Demo @RequestHeader (giữ nguyên từ Bài 4)
    // ================================================================
    // 📘 BÀI 8: @Hidden → endpoint debug nội bộ, KHÔNG hiển thị trên Swagger UI
    @Hidden
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
