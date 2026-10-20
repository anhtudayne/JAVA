package com.example.springbootlearning.client;

import com.example.springbootlearning.dto.external.ExternalPostCreateRequest;
import com.example.springbootlearning.dto.external.ExternalPostResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

/**
 * 📘 BÀI 16 — Declarative HTTP Service Client (Spring 6.0+ / Spring Boot 3.0+)
 *
 * Tương tự như Spring Data JPA Repository:
 * Chỉ cần khai báo Interface với các HTTP Exchange annotations,
 * Spring Framework sẽ tự động sinh mã Dynamic Proxy thực thi tại thời điểm Runtime!
 */
@HttpExchange("/posts")
public interface PostApiClient {

    /**
     * Lấy danh sách tất cả bài viết, hỗ trợ lọc theo userId nếu có.
     */
    @GetExchange
    List<ExternalPostResponse> getAllPosts(@RequestParam(name = "userId", required = false) Long userId);

    /**
     * Lấy bài viết chi tiết theo ID.
     */
    @GetExchange("/{id}")
    ExternalPostResponse getPostById(@PathVariable("id") Long id);

    /**
     * Tạo bài viết mới trên dịch vụ ngoài.
     */
    @PostExchange
    ExternalPostResponse createPost(@RequestBody ExternalPostCreateRequest request);

    /**
     * Cập nhật toàn bộ bài viết (PUT).
     */
    @PutExchange("/{id}")
    ExternalPostResponse updatePost(@PathVariable("id") Long id, @RequestBody ExternalPostCreateRequest request);

    /**
     * Xóa bài viết trên dịch vụ ngoài (DELETE).
     */
    @DeleteExchange("/{id}")
    void deletePost(@PathVariable("id") Long id);
}
