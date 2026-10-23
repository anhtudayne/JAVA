package com.example.springbootlearning.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 📘 BÀI 17: Custom Annotation đo thời gian thực thi method.
 *
 * Đánh dấu method cần đo performance. AOP Aspect sẽ tự động
 * bọc method bằng logic đo thời gian và log kết quả.
 *
 * Ví dụ: @TrackExecutionTime
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackExecutionTime {
}
