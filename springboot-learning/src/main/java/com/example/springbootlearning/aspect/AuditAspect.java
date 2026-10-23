package com.example.springbootlearning.aspect;

import com.example.springbootlearning.annotation.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 📘 BÀI 17: Audit Aspect — Ghi nhận hoạt động nghiệp vụ (Audit Trail).
 *
 * Sử dụng custom annotation @Audited kết hợp @Around advice
 * để tự động ghi log audit cho mọi method quan trọng.
 *
 * Trong production thực tế, audit log thường được ghi vào database
 * hoặc gửi đến hệ thống trung tâm (ELK Stack, Splunk).
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Chặn tất cả method có gắn @Audited.
     *
     * Cú pháp: @annotation(audited) — Spring tự bind annotation instance
     * vào tham số "audited", cho phép đọc thuộc tính action().
     */
    @Around("@annotation(audited)")
    public Object auditAction(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        String action = audited.action();
        String methodName = joinPoint.getSignature().toShortString();
        String args = Arrays.toString(joinPoint.getArgs());
        String timestamp = LocalDateTime.now().format(FORMATTER);

        log.info("📋 [AUDIT] ▶ action={} | method={} | args={} | time={}",
                action, methodName, args, timestamp);

        try {
            Object result = joinPoint.proceed();

            log.info("📋 [AUDIT] ✅ action={} | status=SUCCESS | time={}",
                    action, timestamp);
            return result;

        } catch (Throwable ex) {
            log.error("📋 [AUDIT] ❌ action={} | status=FAILED | error={} | time={}",
                    action, ex.getMessage(), timestamp);
            throw ex;
        }
    }
}
