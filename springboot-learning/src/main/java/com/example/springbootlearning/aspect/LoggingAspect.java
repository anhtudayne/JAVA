package com.example.springbootlearning.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 📘 BÀI 17: Logging Aspect — Tự động log cho toàn bộ Service Layer.
 *
 * Aspect này minh họa tất cả 5 loại Advice trong Spring AOP:
 * - @Before: Log trước khi method chạy
 * - @AfterReturning: Log khi method thành công
 * - @AfterThrowing: Log khi method ném exception
 * - @After: Log sau khi method chạy (dù thành công hay lỗi)
 * - @Around: Bọc toàn bộ method (đo thời gian + log đầu/cuối)
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // ═══════════════════════════════════════════════════════════════
    // 📌 POINTCUT DEFINITIONS — Định nghĩa "nơi áp dụng"
    // ═══════════════════════════════════════════════════════════════

    /**
     * Pointcut: Khớp tất cả public method trong package service.
     *
     * Cú pháp: execution(modifiers? return-type declaring-type? name(params) throws?)
     * - "*"           → bất kỳ return type
     * - "com.example.springbootlearning.service.*" → bất kỳ class trong package service
     * - ".*"          → bất kỳ method name
     * - "(..)"        → bất kỳ số lượng và kiểu tham số
     */
    @Pointcut("execution(* com.example.springbootlearning.service.*.*(..))")
    public void serviceLayer() {
        // Pointcut marker — method body để trống
    }

    /**
     * Pointcut: Khớp tất cả method trong package controller.
     */
    @Pointcut("execution(* com.example.springbootlearning.controller.*.*(..))")
    public void controllerLayer() {
        // Pointcut marker
    }

    // ═══════════════════════════════════════════════════════════════
    // 🟢 @Before — Chạy TRƯỚC khi method target thực thi
    // ═══════════════════════════════════════════════════════════════

    /**
     * Log thông tin đầu vào trước khi controller method được gọi.
     *
     * JoinPoint cung cấp metadata về method đang chạy:
     * - getSignature(): tên class + method
     * - getArgs(): mảng tham số truyền vào
     */
    @Before("controllerLayer()")
    public void logBeforeController(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.info("🔵 [BEFORE] → {} | args: {}", methodName, Arrays.toString(args));
    }

    // ═══════════════════════════════════════════════════════════════
    // 🟢 @AfterReturning — Chạy SAU khi method THÀNH CÔNG
    // ═══════════════════════════════════════════════════════════════

    /**
     * Log kết quả trả về khi service method hoàn thành thành công.
     *
     * Tham số "returning" liên kết với tên biến nhận giá trị return.
     */
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("✅ [AFTER_RETURNING] ← {} | result: {}", methodName, result);
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔴 @AfterThrowing — Chạy SAU khi method NÉM EXCEPTION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Log exception khi service method thất bại.
     *
     * Tham số "throwing" liên kết với tên biến nhận exception.
     * Advice này KHÔNG nuốt exception — nó vẫn propagate bình thường.
     */
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("💥 [AFTER_THROWING] ← {} | exception: {}", methodName, ex.getMessage());
    }

    // ═══════════════════════════════════════════════════════════════
    // ⚫ @After — Chạy SAU khi method kết thúc (giống finally)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Luôn chạy sau khi service method kết thúc,
     * bất kể thành công hay ném exception (giống khối finally).
     */
    @After("serviceLayer()")
    public void logAfterFinally(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("⚫ [AFTER_FINALLY] ← {} completed (success or exception)", methodName);
    }
}
