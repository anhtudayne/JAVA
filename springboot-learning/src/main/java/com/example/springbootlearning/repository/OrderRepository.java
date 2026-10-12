package com.example.springbootlearning.repository;

import com.example.springbootlearning.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 📘 BÀI 10 — Order Repository (Spring Data JPA)
 *
 * Minh họa:
 *   - Derived Query Methods (truy vấn theo thuộc tính đối tượng cha `user.id`).
 *   - Custom JPQL Query với @Query và tham số @Param.
 *   - Native SQL Query.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 1. Tìm tất cả đơn hàng theo ID của User.
     * Spring Data JPA tự động phân tích: 'User' (Entity quan hệ) + 'Id' (Primary Key của User).
     * SQL tương đương: SELECT * FROM orders WHERE user_id = ?;
     */
    List<Order> findByUserId(Long userId);

    /**
     * 2. Tìm đơn hàng theo trạng thái.
     */
    List<Order> findByStatusIgnoreCase(String status);

    /**
     * 3. Kết hợp nhiều điều kiện: User ID và Status.
     */
    List<Order> findByUserIdAndStatusIgnoreCase(Long userId, String status);

    /**
     * 4. Tìm đơn hàng có giá trị lớn hơn hoặc bằng minAmount, sắp xếp giảm dần.
     */
    List<Order> findByTotalAmountGreaterThanEqualOrderByTotalAmountDesc(Double minAmount);

    /**
     * 5. Đếm số đơn hàng theo trạng thái.
     */
    long countByStatusIgnoreCase(String status);

    /**
     * 6. Kiểm tra xem User đã có đơn hàng nào chưa.
     */
    boolean existsByUserId(Long userId);

    /**
     * 7. Custom JPQL Query: Lọc đơn hàng giá trị cao của 1 User cụ thể.
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.totalAmount >= :minAmount ORDER BY o.createdAt DESC")
    List<Order> findHighValueOrdersByUser(@Param("userId") Long userId, @Param("minAmount") Double minAmount);

    /**
     * 8. Custom JPQL Query: Thống kê số lượng đơn hàng và tổng doanh thu theo từng trạng thái.
     */
    @Query("SELECT o.status, COUNT(o), SUM(o.totalAmount) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderSummaryStatistics();

    /**
     * 9. Native SQL Query: Tìm các đơn hàng gần đây (7 ngày).
     */
    @Query(value = "SELECT * FROM orders WHERE created_at >= :sinceDate ORDER BY created_at DESC", nativeQuery = true)
    List<Order> findRecentOrdersNative(@Param("sinceDate") LocalDateTime sinceDate);

    // ===== 10. JOIN FETCH QUERIES (Chống N+1 và tránh LazyInitializationException) =====

    /**
     * Nạp tất cả đơn hàng kèm theo User bằng JOIN FETCH trong 1 câu SQL duy nhất.
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.id DESC")
    List<Order> findAllWithUser();

    /**
     * Nạp đơn hàng theo ID kèm theo User bằng JOIN FETCH.
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
    java.util.Optional<Order> findByIdWithUser(@Param("id") Long id);

    /**
     * Nạp danh sách đơn hàng của một User bằng JOIN FETCH.
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId ORDER BY o.id DESC")
    List<Order> findByUserIdWithUser(@Param("userId") Long userId);

    /**
     * Nạp danh sách đơn hàng theo trạng thái bằng JOIN FETCH.
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE LOWER(o.status) = LOWER(:status) ORDER BY o.id DESC")
    List<Order> findByStatusWithUser(@Param("status") String status);
}
