package com.example.springbootlearning.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 📘 BÀI 17: Performance Aspect — Đo thời gian thực thi method.
 *
 * Sử dụng @Around advice kết hợp custom annotation @TrackExecutionTime
 * để đo và log thời gian thực thi của bất kỳ method nào được đánh dấu.
 *
 * Cách dùng: Gắn @TrackExecutionTime lên method cần đo.
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    /**
     * @Around advice bọc quanh method có annotation @TrackExecutionTime.
     *
     * ProceedingJoinPoint (chỉ dùng trong @Around) cho phép:
     * - proceed(): gọi method thật (bắt buộc, nếu không method sẽ không chạy)
     * - getSignature(): lấy tên method
     * - getArgs(): lấy tham số
     *
     * Flow: [Trước proceed()] → [Method thật chạy] → [Sau proceed()]
     */
    @Around("@annotation(com.example.springbootlearning.annotation.TrackExecutionTime)")
    public Object trackTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        long startTime = System.nanoTime();
        log.info("⏱️ [PERF] ▶ Starting: {}", methodName);

        try {
            Object result = joinPoint.proceed(); // Gọi method thật

            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info("⏱️ [PERF] ◀ Completed: {} — took {}ms", methodName, elapsedMs);
            return result;

        } catch (Throwable ex) {
            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("⏱️ [PERF] 💥 Failed: {} — took {}ms — error: {}",
                    methodName, elapsedMs, ex.getMessage());
            throw ex; // Ném lại exception — không nuốt
        }
    }
}
