package com.example.springbootlearning.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minh hoạ Bean Lifecycle:
 * - @PostConstruct: Chạy SAU KHI constructor hoàn thành và dependencies đã được inject đầy đủ.
 * - @PreDestroy: Chạy TRƯỚC KHI Bean bị huỷ (khi ứng dụng shutdown).
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public CacheService() {
        log.info("1️⃣ [CacheService] Constructor được gọi - Bean đang được khởi tạo.");
    }

    @PostConstruct
    public void init() {
        log.info("4️⃣ [CacheService] @PostConstruct: Nạp dữ liệu ban đầu vào cache (Warm-up cache)...");
        cache.put("system.status", "RUNNING");
        cache.put("system.started_at", String.valueOf(System.currentTimeMillis()));
        cache.put("system.default_lang", "vi_VN");
        log.info("🟢 [CacheService] @PostConstruct hoàn tất! Đã nạp {} items vào cache.", cache.size());
    }

    @PreDestroy
    public void cleanup() {
        log.info("7️⃣ [CacheService] @PreDestroy: Ứng dụng đang tắt, giải phóng tài nguyên cache...");
        cache.clear();
        log.info("🔴 [CacheService] @PreDestroy hoàn tất! Đã dọn dẹp cache sạch sẽ.");
    }

    public String get(String key) {
        return cache.getOrDefault(key, "KEY_NOT_FOUND");
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }

    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(cache);
    }

    public int size() {
        return cache.size();
    }
}
