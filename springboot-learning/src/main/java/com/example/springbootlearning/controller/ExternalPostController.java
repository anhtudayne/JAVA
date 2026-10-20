package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.external.ExternalPostCreateRequest;
import com.example.springbootlearning.dto.external.ExternalPostResponse;
import com.example.springbootlearning.dto.response.ApiResponse;
import com.example.springbootlearning.service.ExternalPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 📘 BÀI 16 — Controller thử nghiệm gọi External API
 */
@Tag(name = "🌐 External Posts API", description = "Tích hợp và gọi API ngoài (JSONPlaceholder) qua RestClient & HTTP Interfaces")
@RestController
@RequestMapping("/api/v1/external-posts")
public class ExternalPostController {

    private final ExternalPostService externalPostService;

    public ExternalPostController(ExternalPostService externalPostService) {
        this.externalPostService = externalPostService;
    }

    @Operation(summary = "Lấy danh sách bài viết từ External API (Declarative Client)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExternalPostResponse>>> getAllPosts(
            @RequestParam(required = false) Long userId
    ) {
        List<ExternalPostResponse> posts = externalPostService.getAllPostsDeclarative(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài viết external thành công", posts));
    }

    @Operation(summary = "Lấy chi tiết bài viết theo ID (Declarative Client)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExternalPostResponse>> getPostById(@PathVariable Long id) {
        ExternalPostResponse post = externalPostService.getPostByIdDeclarative(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy bài viết external thành công", post));
    }

    @Operation(summary = "Lấy chi tiết bài viết với RestClient Fluent API (Error Handling sâu)")
    @GetMapping("/fluent/{id}")
    public ResponseEntity<ApiResponse<ExternalPostResponse>> getPostByIdFluent(@PathVariable Long id) {
        ExternalPostResponse post = externalPostService.getPostWithDetailedErrorHandling(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy bài viết qua RestClient Fluent thành công", post));
    }

    @Operation(summary = "Tạo bài viết mới trên External API")
    @PostMapping
    public ResponseEntity<ApiResponse<ExternalPostResponse>> createPost(
            @Valid @RequestBody ExternalPostCreateRequest request
    ) {
        ExternalPostResponse created = externalPostService.createPostDeclarative(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo bài viết external thành công", created));
    }

    @Operation(summary = "Cập nhật bài viết trên External API (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExternalPostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody ExternalPostCreateRequest request
    ) {
        ExternalPostResponse updated = externalPostService.updatePostDeclarative(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết external thành công", updated));
    }

    @Operation(summary = "Xóa bài viết trên External API (DELETE)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        externalPostService.deletePostDeclarative(id);
        return ResponseEntity.noContent().build();
    }
}
