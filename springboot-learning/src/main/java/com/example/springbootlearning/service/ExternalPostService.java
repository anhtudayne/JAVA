package com.example.springbootlearning.service;

import com.example.springbootlearning.client.PostApiClient;
import com.example.springbootlearning.dto.external.ExternalPostCreateRequest;
import com.example.springbootlearning.dto.external.ExternalPostResponse;
import com.example.springbootlearning.exception.ExternalApiException;
import com.example.springbootlearning.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 📘 BÀI 16 — Service tích hợp External API
 *
 * So sánh song song:
 *   - Cách 1: Sử dụng Declarative HTTP Client (PostApiClient) — Code siêu gọn, tự nhiên như gọi Java method.
 *   - Cách 2: Sử dụng RestClient Fluent API — Cho phép can thiệp chi tiết onStatus, kiểm soát ngoại lệ linh hoạt.
 */
@Service
public class ExternalPostService {

    private static final Logger log = LoggerFactory.getLogger(ExternalPostService.class);

    private final PostApiClient postApiClient;
    private final RestClient jsonPlaceholderRestClient;

    public ExternalPostService(PostApiClient postApiClient, RestClient jsonPlaceholderRestClient) {
        this.postApiClient = postApiClient;
        this.jsonPlaceholderRestClient = jsonPlaceholderRestClient;
    }

    // ========================================================
    // CÁCH 1: DÙNG DECLARATIVE HTTP SERVICE CLIENT (Khuyến nghị)
    // ========================================================

    /**
     * Lấy danh sách tất cả bài viết từ dịch vụ ngoài qua Interface Declarative.
     */
    public List<ExternalPostResponse> getAllPostsDeclarative(Long userId) {
        log.info("📥 [Declarative] Lấy danh sách external posts - userId={}", userId);
        return postApiClient.getAllPosts(userId);
    }

    /**
     * Lấy bài viết theo ID qua Interface Declarative.
     */
    public ExternalPostResponse getPostByIdDeclarative(Long id) {
        log.info("📥 [Declarative] Lấy external post theo id={}", id);
        try {
            return postApiClient.getPostById(id);
        } catch (Exception ex) {
            log.error("❌ Lỗi khi gọi external post id={}: {}", id, ex.getMessage());
            throw new ResourceNotFoundException("External Post", "id", id);
        }
    }

    /**
     * Tạo bài viết mới qua Interface Declarative.
     */
    public ExternalPostResponse createPostDeclarative(ExternalPostCreateRequest request) {
        log.info("📥 [Declarative] Tạo mới external post: title='{}'", request.title());
        return postApiClient.createPost(request);
    }

    /**
     * Cập nhật bài viết qua Interface Declarative.
     */
    public ExternalPostResponse updatePostDeclarative(Long id, ExternalPostCreateRequest request) {
        log.info("📥 [Declarative] Cập nhật external post id={}", id);
        return postApiClient.updatePost(id, request);
    }

    /**
     * Xóa bài viết qua Interface Declarative.
     */
    public void deletePostDeclarative(Long id) {
        log.info("📥 [Declarative] Xóa external post id={}", id);
        postApiClient.deletePost(id);
    }

    // ========================================================
    // CÁCH 2: DÙNG RESTCLIENT FLUENT API TRỰC TIẾP (Error Handling sâu)
    // ========================================================

    /**
     * Lấy chi tiết bài viết với cơ chế bắt lỗi HTTP chi tiết bằng onStatus.
     */
    public ExternalPostResponse getPostWithDetailedErrorHandling(Long id) {
        log.info("📥 [RestClient Fluent] Lấy external post với error handling id={}", id);
        return jsonPlaceholderRestClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    log.warn("⚠️ API ngoài trả về 404 cho post id={}", id);
                    throw new ResourceNotFoundException("External Post", "id", id);
                })
                .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                    String errorText = new String(resp.getBody().readAllBytes());
                    log.error("❌ Client Error từ API ngoài: {}", errorText);
                    throw new ExternalApiException(resp.getStatusCode().value(), "Lỗi yêu cầu từ phía client tới dịch vụ ngoài: " + errorText);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    log.error("❌ Server Error từ dịch vụ ngoài!");
                    throw new ExternalApiException(502, "Dịch vụ bên ngoài đang gặp sự cố gián đoạn!");
                })
                .body(ExternalPostResponse.class);
    }

    /**
     * Lấy danh sách bài viết dùng ParameterizedTypeReference tránh Type Erasure.
     */
    public List<ExternalPostResponse> getAllPostsViaFluentApi() {
        log.info("📥 [RestClient Fluent] Lấy toàn bộ danh sách posts");
        return jsonPlaceholderRestClient.get()
                .uri("/posts")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExternalPostResponse>>() {});
    }
}
