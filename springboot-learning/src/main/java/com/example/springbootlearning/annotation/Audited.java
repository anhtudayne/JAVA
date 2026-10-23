package com.example.springbootlearning.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 📘 BÀI 17: Custom Annotation cho AOP Audit.
 *
 * Đánh dấu method cần ghi nhận hoạt động (audit trail).
 * AOP Aspect sẽ tự động chặn và log thông tin mỗi khi method được gọi.
 *
 * Ví dụ: @Audited(action = "DELETE_USER")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * Mô tả hành động nghiệp vụ (ví dụ: "CREATE_USER", "DELETE_ORDER").
     */
    String action() default "";
}
