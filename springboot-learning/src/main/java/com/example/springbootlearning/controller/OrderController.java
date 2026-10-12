package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.request.OrderCreateRequest;
import com.example.springbootlearning.dto.response.ApiResponse;
import com.example.springbootlearning.dto.response.OrderResponse;
import com.example.springbootlearning.model.AuditLog;
import com.example.springbootlearning.service.AuditLogService;
import com.example.springbootlearning.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 📘 BÀI 12 — Order REST Controller (Transaction Management API)
 */
@Tag(name = "🧾 Order Management", description = "API quản lý đơn hàng & kiểm thử Transaction Management (@Transactional, ACID, Rollback, Propagation)")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final AuditLogService auditLogService;

    public OrderController(OrderService orderService, AuditLogService auditLogService) {
        this.orderService = orderService;
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "Tạo đơn hàng mới (Minh họa Transaction: Lưu Order + Giảm tồn kho nguyên tử)")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        log.info("📥 REST: Tạo đơn hàng cho userId={}, productId={}, qty={}",
                request.userId(), request.productId(), request.quantity());

        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo đơn hàng thành công", response));
    }

    @Operation(summary = "Lấy tất cả các đơn hàng")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tất cả đơn hàng thành công", orders));
    }

    @Operation(summary = "Lấy chi tiết đơn hàng theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @Parameter(description = "ID của đơn hàng") @PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đơn hàng thành công", order));
    }

    @Operation(summary = "Lấy danh sách đơn hàng của một người dùng")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUser(
            @Parameter(description = "ID người dùng") @PathVariable Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng của người dùng thành công", orders));
    }

    @Operation(summary = "Lọc danh sách đơn hàng theo trạng thái (CONFIRMED, DELIVERED, CANCELLED...)")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @Parameter(description = "Trạng thái đơn hàng") @PathVariable String status) {
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Lọc danh sách đơn hàng theo trạng thái thành công", orders));
    }

    @Operation(summary = "Hủy đơn hàng")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(description = "ID của đơn hàng cần hủy") @PathVariable Long id) {
        OrderResponse cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy đơn hàng thành công", cancelledOrder));
    }

    @Operation(summary = "Xem danh sách Audit Log (Minh họa dữ liệu được persist độc lập bởi Propagation.REQUIRES_NEW)")
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        List<AuditLog> auditLogs = auditLogService.getAllAuditLogs();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách Audit Log thành công", auditLogs));
    }
}
