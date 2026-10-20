package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.request.ProductCreateRequest;
import com.example.springbootlearning.dto.response.ProductResponse;
import com.example.springbootlearning.exception.ResourceNotFoundException;
import com.example.springbootlearning.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 📘 BÀI 14 — Slice Test cho ProductController sử dụng @WebMvcTest & MockMvc
 *
 * Kiểm tra:
 *   - Chuẩn REST Response với ApiResponse wrapper
 *   - HTTP Status 200 OK, 201 Created, 204 No Content
 *   - Xử lý ngoại lệ ResourceNotFoundException qua GlobalExceptionHandler
 */
@WebMvcTest(ProductController.class)
@DisplayName("Kiểm thử ProductController với WebMvcTest & MockMvc")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/v1/products: Trả về HTTP 200 và danh sách sản phẩm bọc trong ApiResponse")
    void getAllProducts_ShouldReturnApiResponseWithProducts() throws Exception {
        // Given
        ProductResponse p1 = new ProductResponse(1L, "MacBook Pro", "M3 Max", 3499.0, "Laptop", 10, LocalDateTime.now(), LocalDateTime.now());
        ProductResponse p2 = new ProductResponse(2L, "iPhone 16", "Pro Max", 1199.0, "Smartphone", 50, LocalDateTime.now(), LocalDateTime.now());

        when(productService.getAllProducts()).thenReturn(List.of(p1, p2));

        // When & Then
        mockMvc.perform(get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("MacBook Pro")))
                .andExpect(jsonPath("$.data[1].name", is("iPhone 16")));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/v1/products/1: Trả về HTTP 200 khi sản phẩm tồn tại")
    void getProductById_WhenExists_ShouldReturnProduct() throws Exception {
        // Given
        ProductResponse response = new ProductResponse(1L, "MacBook Pro", "M3 Max", 3499.0, "Laptop", 10, LocalDateTime.now(), LocalDateTime.now());
        when(productService.getProductById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("MacBook Pro")));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/products/999: Trả về HTTP 404 khi sản phẩm không tìm thấy")
    void getProductById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(productService.getProductById(999L)).thenThrow(new ResourceNotFoundException("Product", "id", 999L));

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message").exists());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    @DisplayName("POST /api/v1/products: Trả về HTTP 201 Created khi tạo sản phẩm thành công")
    void createProduct_WithValidBody_ShouldReturn201() throws Exception {
        // Given
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Bàn phím cơ Custom");
        request.setDescription("Switch Linear");
        request.setPrice(150.0);
        request.setCategory("Phụ kiện");
        request.setStock(30);

        ProductResponse response = new ProductResponse(10L, "Bàn phím cơ Custom", "Switch Linear", 150.0, "Phụ kiện", 30, LocalDateTime.now(), LocalDateTime.now());
        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.data.id", is(10)))
                .andExpect(jsonPath("$.data.name", is("Bàn phím cơ Custom")));

        verify(productService, times(1)).createProduct(any(ProductCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/products: Trả về HTTP 400 Bad Request khi payload vi phạm Bean Validation")
    void createProduct_WithInvalidBody_ShouldReturn400() throws Exception {
        // Given: body thiếu name, price null, category rỗng
        ProductCreateRequest invalidRequest = new ProductCreateRequest();
        invalidRequest.setName("");
        invalidRequest.setPrice(null);
        invalidRequest.setCategory("");

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message").exists());

        // Đảm bảo không bao giờ gọi tới service layer khi validation fail
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("DELETE /api/v1/products/1: Trả về HTTP 204 No Content khi xóa thành công")
    void deleteProduct_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/products/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }
}
