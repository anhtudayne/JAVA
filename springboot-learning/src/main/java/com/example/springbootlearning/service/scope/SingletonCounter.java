package com.example.springbootlearning.service.scope;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minh hoạ Singleton Scope (Mặc định trong Spring):
 * Chỉ tồn tại DUY NHẤT 1 instance trong toàn bộ ApplicationContext.
 * Tất cả request/class inject vào đều dùng chung instance này.
 */
@Component
public class SingletonCounter {

    // Dùng AtomicInteger để thread-safe khi nhiều request truy cập đồng thời vào Singleton bean
    private final AtomicInteger count = new AtomicInteger(0);

    public int incrementAndGet() {
        return count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }

    public String getInstanceId() {
        return "SingletonInstance@" + Integer.toHexString(System.identityHashCode(this));
    }
}
