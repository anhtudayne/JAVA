package com.example.springbootlearning.client;

import com.example.springbootlearning.dto.external.ExternalPostCreateRequest;
import com.example.springbootlearning.dto.external.ExternalPostResponse;
import com.example.springbootlearning.exception.ResourceNotFoundException;
import com.example.springbootlearning.service.ExternalPostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 📘 BÀI 16 — Unit Test cho ExternalPostService sử dụng Mockito
 *
 * Kiểm thử logic nghiệp vụ của ExternalPostService khi tương tác với PostApiClient.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalPostService - Unit Test")
class ExternalPostServiceTest {

    @Mock
    private PostApiClient postApiClient;

    @Mock
    private RestClient jsonPlaceholderRestClient;

    @InjectMocks
    private ExternalPostService externalPostService;

    @Test
    @DisplayName("Lấy danh sách tất cả posts qua Declarative client thành công")
    void testGetAllPostsDeclarative_Success() {
        // Given
        List<ExternalPostResponse> mockList = List.of(
                new ExternalPostResponse(1L, 1L, "Post 1", "Body 1"),
                new ExternalPostResponse(2L, 1L, "Post 2", "Body 2")
        );
        when(postApiClient.getAllPosts(null)).thenReturn(mockList);

        // When
        List<ExternalPostResponse> result = externalPostService.getAllPostsDeclarative(null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Post 1");
        verify(postApiClient, times(1)).getAllPosts(null);
    }

    @Test
    @DisplayName("Lấy post theo ID qua Declarative client thành công")
    void testGetPostByIdDeclarative_Success() {
        // Given
        ExternalPostResponse mockPost = new ExternalPostResponse(10L, 2L, "Title 10", "Content 10");
        when(postApiClient.getPostById(10L)).thenReturn(mockPost);

        // When
        ExternalPostResponse result = externalPostService.getPostByIdDeclarative(10L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("Title 10");
    }

    @Test
    @DisplayName("Lấy post theo ID gặp lỗi từ client ngoài sẽ ném ResourceNotFoundException")
    void testGetPostByIdDeclarative_NotFound() {
        // Given
        when(postApiClient.getPostById(999L)).thenThrow(new RuntimeException("404 Not Found"));

        // When & Then
        assertThatThrownBy(() -> externalPostService.getPostByIdDeclarative(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("External Post");
    }

    @Test
    @DisplayName("Tạo mới post qua Declarative client thành công")
    void testCreatePostDeclarative_Success() {
        // Given
        ExternalPostCreateRequest request = new ExternalPostCreateRequest("Tiêu đề mới", "Nội dung mới", 1L);
        ExternalPostResponse response = new ExternalPostResponse(101L, 1L, "Tiêu đề mới", "Nội dung mới");
        when(postApiClient.createPost(any(ExternalPostCreateRequest.class))).thenReturn(response);

        // When
        ExternalPostResponse created = externalPostService.createPostDeclarative(request);

        // Then
        assertThat(created.id()).isEqualTo(101L);
        assertThat(created.title()).isEqualTo("Tiêu đề mới");
        verify(postApiClient, times(1)).createPost(request);
    }

    @Test
    @DisplayName("Cập nhật post qua Declarative client thành công")
    void testUpdatePostDeclarative_Success() {
        // Given
        ExternalPostCreateRequest request = new ExternalPostCreateRequest("Tiêu đề sửa", "Nội dung sửa", 1L);
        ExternalPostResponse response = new ExternalPostResponse(5L, 1L, "Tiêu đề sửa", "Nội dung sửa");
        when(postApiClient.updatePost(eq(5L), any(ExternalPostCreateRequest.class))).thenReturn(response);

        // When
        ExternalPostResponse updated = externalPostService.updatePostDeclarative(5L, request);

        // Then
        assertThat(updated.title()).isEqualTo("Tiêu đề sửa");
        verify(postApiClient, times(1)).updatePost(5L, request);
    }

    @Test
    @DisplayName("Xóa post qua Declarative client thành công")
    void testDeletePostDeclarative_Success() {
        // Given
        doNothing().when(postApiClient).deletePost(1L);

        // When
        externalPostService.deletePostDeclarative(1L);

        // Then
        verify(postApiClient, times(1)).deletePost(1L);
    }
}
