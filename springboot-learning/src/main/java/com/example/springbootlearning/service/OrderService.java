package com.example.springbootlearning.service;

import com.example.springbootlearning.dto.request.OrderCreateRequest;
import com.example.springbootlearning.dto.response.OrderResponse;
import com.example.springbootlearning.exception.ResourceNotFoundException;
import com.example.springbootlearning.model.Order;
import com.example.springbootlearning.model.Product;
import com.example.springbootlearning.model.User;
import com.example.springbootlearning.repository.OrderRepository;
import com.example.springbootlearning.repository.ProductRepository;
import com.example.springbootlearning.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 📘 BÀI 12 — OrderService (Transaction Management)
 *
 * Minh họa các kiến thức chuyên sâu về Transaction trong Spring:
 *   ✅ @Transactional đảm bảo tính Atomicity (All-or-Nothing): Tạo đơn hàng + Giảm tồn kho.
 *   ✅ @Transactional(readOnly = true) ở Class-level: Tối ưu hiệu năng truy vấn đọc (skip dirty checking).
 *   ✅ rollbackFor = Exception.class: Rollback an toàn cho cả Checked và Unchecked Exceptions.
 *   ✅ Propagation.REQUIRES_NEW qua AuditLogService: Ghi nhận vết hệ thống độc lập.
 *   ✅ Atomic SQL Update: Chống Race Condition khi nhiều giao dịch đồng thời trừ kho.
 */
@Service
@Transactional(readOnly = true) // Mặc định: tất cả phương thức trong Service này là READ-ONLY
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        log.info("✅ OrderService đã được khởi tạo với Transaction Management");
    }

    // ================================================================
    // WRITE OPERATIONS — Cần Transaction ghi (readOnly = false)
    // ================================================================

    /**
     * Tạo mới đơn hàng với cam kết toàn vẹn dữ liệu (ACID).
     *
     * Quy trình:
     *   1. Xác thực User tồn tại.
     *   2. Xác thực Product tồn tại.
     *   3. Kiểm tra số lượng tồn kho khả dụng.
     *   4. Lưu đơn hàng mới với trạng thái "CONFIRMED".
     *   5. Ghi nhận AuditLog độc lập qua REQUIRES_NEW.
     *   6. Giảm tồn kho nguyên tử qua câu lệnh SQL Update có điều kiện.
     *
     * ⚠️ Nếu bước 6 thất bại (hết hàng do race condition) -> Ném exception -> ROLLBACK đơn hàng ở bước 4!
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderCreateRequest request) {
        log.info("📥 [TX-START] Bắt đầu quy trình tạo đơn hàng: userId={}, productId={}, quantity={}",
                request.userId(), request.productId(), request.quantity());

        // ── Bước 1: Validate User tồn tại ──
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.warn("⚠️ User không tồn tại: id={}", request.userId());
                    return new ResourceNotFoundException("User", "id", request.userId());
                });

        // ── Bước 2: Validate Product tồn tại ──
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tồn tại: id={}", request.productId());
                    return new ResourceNotFoundException("Product", "id", request.productId());
                });

        // ── Bước 3: Kiểm tra tồn kho sơ bộ ──
        if (product.getStock() < request.quantity()) {
            log.warn("⚠️ Tồn kho không đủ: hiện có={}, yêu cầu={}", product.getStock(), request.quantity());
            throw new IllegalStateException(String.format(
                    "Không đủ hàng trong kho. Sản phẩm '%s' hiện còn %d, yêu cầu %d.",
                    product.getName(), product.getStock(), request.quantity()
            ));
        }

        // ── Bước 4: Tạo và lưu Order ──
        double totalAmount = product.getPrice() * request.quantity();
        String description = (request.description() != null && !request.description().isBlank())
                ? request.description()
                : "Đơn mua " + product.getName() + " (SL: " + request.quantity() + ")";

        Order order = new Order(description, totalAmount, "CONFIRMED", user);
        order = orderRepository.save(order);
        log.info("💾 Đơn hàng tạm thời được lưu: orderId={}, totalAmount={}", order.getId(), totalAmount);

        // ── Bước 5: Ghi AuditLog độc lập (REQUIRES_NEW) ──
        auditLogService.logAction(
                "CREATE_ORDER_ATTEMPT",
                "Khởi tạo đơn hàng #" + order.getId() + " cho khách hàng " + user.getName(),
                order.getId()
        );

        // ── Bước 6: Giảm tồn kho nguyên tử (Atomic Update chống Race Condition) ──
        int updatedRows = productRepository.reduceStockAtomic(product.getId(), request.quantity());
        if (updatedRows == 0) {
            // Race condition: khách hàng khác đã mua hết tồn kho trong tích tắc
            log.error("💥 Race condition phát hiện: Tồn kho của productId={} không đủ để trừ {} sản phẩm!",
                    product.getId(), request.quantity());
            throw new IllegalStateException(
                    "Sản phẩm đã hết hàng trong quá trình xử lý đơn hàng. Giao dịch bị hủy bỏ!"
            );
            // 💥 RuntimeException tại đây kích hoạt ROLLBACK toàn bộ thay đổi ở Bước 4!
        }

        log.info("🎉 [TX-COMMIT] Đơn hàng hoàn tất thành công: orderId={}, productId={}, stockUpdatedRows={}",
                order.getId(), product.getId(), updatedRows);
        return OrderResponse.fromEntity(order);
    }

    /**
     * Hủy đơn hàng và cập nhật trạng thái.
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(Long orderId) {
        log.info("📥 [TX-START] Yêu cầu hủy đơn hàng: orderId={}", orderId);

        Order order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Đơn hàng #" + orderId + " đã bị hủy từ trước!");
        }

        order.setStatus("CANCELLED");
        order = orderRepository.save(order);

        auditLogService.logAction(
                "CANCEL_ORDER",
                "Đã hủy đơn hàng #" + orderId,
                orderId
        );

        log.info("✅ [TX-COMMIT] Đã hủy đơn hàng thành công: orderId={}", orderId);
        return OrderResponse.fromEntity(order);
    }

    // ================================================================
    // READ OPERATIONS — Thừa hưởng @Transactional(readOnly = true)
    // ================================================================

    public List<OrderResponse> getAllOrders() {
        log.debug("Lấy toàn bộ danh sách đơn hàng (JOIN FETCH với User)");
        return orderRepository.findAllWithUser().stream()
                .map(OrderResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        log.debug("Tìm đơn hàng theo id={}", id);
        Order order = orderRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return OrderResponse.fromEntity(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        log.debug("Lấy danh sách đơn hàng của userId={}", userId);
        return orderRepository.findByUserIdWithUser(userId).stream()
                .map(OrderResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(String status) {
        log.debug("Lọc danh sách đơn hàng theo status={}", status);
        return orderRepository.findByStatusWithUser(status).stream()
                .map(OrderResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
