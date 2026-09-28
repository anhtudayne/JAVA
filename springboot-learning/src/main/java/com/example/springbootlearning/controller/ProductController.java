package com.example.springbootlearning.controller;

import com.example.springbootlearning.model.Product;
import com.example.springbootlearning.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 📘 BÀI 4 — Product REST Controller
 *
 * Minh hoạ toàn bộ kiến thức Spring MVC:
 * 1. @RestController — Trả JSON tự động (nhờ @ResponseBody + Jackson)
 * 2. @RequestMapping("/api/v1/products") — Prefix URL cho tất cả endpoints
 * 3. Đầy đủ 5 HTTP Methods: GET, POST, PUT, PATCH, DELETE
 * 4. 4 Annotation nhận data: @PathVariable, @RequestParam, @RequestBody, @RequestHeader
 *
 * 🔑 Controller chỉ làm 3 việc:
 *    ① Nhận request
 *    ② Gọi Service xử lý
 *    ③ Trả response
 *
 * Luồng: Client → DispatcherServlet → ProductController → ProductService → ProductRepository
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
    // ① GET — Lấy tất cả sản phẩm (có hỗ trợ filter bằng @RequestParam)
    // ================================================================
    //
    // GET /api/v1/products                            → Lấy tất cả
    // GET /api/v1/products?category=Laptop             → Lọc theo category
    // GET /api/v1/products?minPrice=500&maxPrice=2000   → Lọc theo khoảng giá
    //
    // @RequestParam: Lấy giá trị từ query string (?key=value)
    //   - required = false  → param không bắt buộc, có thể thiếu trên URL
    //   - Khi param thiếu, giá trị sẽ là null
    //
    @GetMapping
    public List<Product> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        log.info("📥 GET /api/v1/products — category={}, minPrice={}, maxPrice={}",
                category, minPrice, maxPrice);

        // Nếu có filter category → lọc theo category
        if (category != null && !category.isBlank()) {
            return productService.getByCategory(category);
        }

        // Nếu có filter giá → lọc theo khoảng giá
        if (minPrice != null && maxPrice != null) {
            return productService.getByPriceRange(minPrice, maxPrice);
        }

        // Mặc định: trả về tất cả
        return productService.getAllProducts();
    }

    // ================================================================
    // ② GET — Lấy sản phẩm theo ID (dùng @PathVariable)
    // ================================================================
    //
    // GET /api/v1/products/42
    //
    // @PathVariable: Lấy giá trị "{id}" từ URL path
    //   - Spring tự động parse String "42" → Long 42L
    //   - Nếu URL không match → DispatcherServlet trả 404
    //
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        log.info("📥 GET /api/v1/products/{} — Tìm product theo ID", id);
        return productService.getProductById(id);
    }

    // ================================================================
    // ③ GET — Tìm kiếm sản phẩm theo tên (dùng @RequestParam)
    // ================================================================
    //
    // GET /api/v1/products/search?keyword=macbook
    //
    // Lưu ý: Endpoint "/search" được khai báo TRƯỚC "/{id}"
    // để Spring không nhầm "search" là giá trị của {id}.
    // (Thực tế Spring xử lý đúng, nhưng đặt cụ thể hơn trước là best practice)
    //
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        log.info("📥 GET /api/v1/products/search — keyword='{}'", keyword);
        return productService.searchByName(keyword);
    }

    // ================================================================
    // ④ POST — Tạo sản phẩm mới (dùng @RequestBody)
    // ================================================================
    //
    // POST /api/v1/products
    // Content-Type: application/json
    // Body: {"name": "iPad Pro", "description": "Tablet", "price": 1099, "category": "Tablet", "stock": 50}
    //
    // @RequestBody: Jackson tự động chuyển JSON body → Product object
    //   Cơ chế hoạt động:
    //   1. Jackson đọc JSON string từ request body
    //   2. Tạo new Product() (gọi no-args constructor)
    //   3. Gọi setName("iPad Pro"), setPrice(1099)... (gọi setters)
    //   4. Truyền Product object vào tham số method
    //
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        log.info("📥 POST /api/v1/products — Tạo product mới: {}", product.getName());
        return productService.createProduct(product);
    }

    // ================================================================
    // ⑤ PUT — Cập nhật TOÀN BỘ sản phẩm (Replace)
    // ================================================================
    //
    // PUT /api/v1/products/42
    // Body: {"name": "MacBook Pro M4", "description": "Updated", "price": 2999, "category": "Laptop", "stock": 20}
    //
    // Kết hợp cả @PathVariable (id từ URL) và @RequestBody (data từ body)
    //
    // PUT = Thay thế toàn bộ:
    //   - Field nào KHÔNG gửi sẽ trở thành null
    //   - Phải gửi đầy đủ tất cả field
    //
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        log.info("📥 PUT /api/v1/products/{} — Cập nhật toàn bộ product", id);
        return productService.updateProduct(id, product);
    }

    // ================================================================
    // ⑥ PATCH — Cập nhật MỘT PHẦN sản phẩm (Partial Update)
    // ================================================================
    //
    // PATCH /api/v1/products/42
    // Body: {"price": 1999}            → Chỉ đổi giá, giữ nguyên name, category...
    // Body: {"stock": 0, "price": 899} → Đổi stock + price, giữ nguyên phần còn lại
    //
    // PATCH khác PUT:
    //   - PUT thay thế TOÀN BỘ → field thiếu = null
    //   - PATCH chỉ cập nhật field CÓ TRONG body → field thiếu = giữ nguyên
    //
    @PatchMapping("/{id}")
    public Product patchProduct(@PathVariable Long id, @RequestBody Product partialUpdate) {
        log.info("📥 PATCH /api/v1/products/{} — Cập nhật một phần product", id);
        return productService.patchProduct(id, partialUpdate);
    }

    // ================================================================
    // ⑦ DELETE — Xóa sản phẩm
    // ================================================================
    //
    // DELETE /api/v1/products/42
    //
    // Trả về Map thay vì String thuần để kết quả là JSON có cấu trúc.
    // (Bài 5 sẽ học ResponseEntity để kiểm soát HTTP Status Code chuẩn hơn)
    //
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteProduct(@PathVariable Long id) {
        log.info("📥 DELETE /api/v1/products/{}", id);
        productService.deleteProduct(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Đã xóa product thành công");
        response.put("deletedId", id);
        return response;
    }

    // ================================================================
    // ⑧ GET — Demo @RequestHeader (đọc metadata từ HTTP Header)
    // ================================================================
    //
    // GET /api/v1/products/debug/headers
    // Headers:
    //   User-Agent: PostmanRuntime/7.x
    //   Accept-Language: vi
    //
    @GetMapping("/debug/headers")
    public Map<String, String> debugHeaders(
            @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            @RequestHeader(value = "X-Custom-Header", required = false) String customHeader
    ) {
        log.info("📥 GET /api/v1/products/debug/headers — Reading HTTP headers");

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", userAgent);
        headers.put("Accept-Language", language);
        headers.put("X-Custom-Header", customHeader != null ? customHeader : "Không có (header tùy chỉnh)");
        headers.put("explanation", "Dữ liệu này được đọc từ HTTP Headers bằng @RequestHeader");
        return headers;
    }
}
