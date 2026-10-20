package com.example.springbootlearning.service;

import com.example.springbootlearning.dto.request.ProductCreateRequest;
import com.example.springbootlearning.dto.request.ProductUpdateRequest;
import com.example.springbootlearning.dto.response.ProductResponse;
import com.example.springbootlearning.exception.DuplicateResourceException;
import com.example.springbootlearning.exception.ResourceNotFoundException;
import com.example.springbootlearning.model.Product;
import com.example.springbootlearning.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 📘 BÀI 14 — Unit Test cho ProductService với JUnit 5 & Mockito
 *
 * Kiểm tra các tình huống nghiệp vụ:
 *   - Lấy chi tiết sản phẩm (thành công vs ResourceNotFoundException)
 *   - Tạo sản phẩm mới (thành công vs DuplicateResourceException)
 *   - Cập nhật thông tin sản phẩm (PUT)
 *   - Xóa sản phẩm
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử ProductService với JUnit 5 & Mockito")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("MacBook Pro M3");
        sampleProduct.setDescription("Laptop Apple chip M3");
        sampleProduct.setPrice(2499.99);
        sampleProduct.setCategory("Laptop");
        sampleProduct.setStock(15);
    }

    @Nested
    @DisplayName("Kịch bản getProductById")
    class GetProductByIdTests {

        @Test
        @DisplayName("Thành công: Trả về ProductResponse khi ID hợp lệ")
        void getProductById_WhenProductExists_ReturnsProductResponse() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            // When
            ProductResponse response = productService.getProductById(1L);

            // Then
            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("MacBook Pro M3", response.name());
            assertEquals(2499.99, response.price());
            verify(productRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Thất bại: Ném ResourceNotFoundException khi ID không tồn tại")
        void getProductById_WhenProductNotFound_ThrowsException() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
            verify(productRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("Kịch bản createProduct")
    class CreateProductTests {

        @Test
        @DisplayName("Thành công: Tạo mới sản phẩm khi tên chưa tồn tại")
        void createProduct_WithUniqueName_ReturnsSavedProduct() {
            // Given
            ProductCreateRequest request = new ProductCreateRequest();
            request.setName("iPad Pro M4");
            request.setDescription("Tablet cao cấp");
            request.setPrice(999.0);
            request.setCategory("Tablet");
            request.setStock(20);

            Product savedProduct = new Product();
            savedProduct.setId(2L);
            savedProduct.setName("iPad Pro M4");
            savedProduct.setDescription("Tablet cao cấp");
            savedProduct.setPrice(999.0);
            savedProduct.setCategory("Tablet");
            savedProduct.setStock(20);

            when(productRepository.existsByNameIgnoreCase("iPad Pro M4")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // When
            ProductResponse result = productService.createProduct(request);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.id());
            assertEquals("iPad Pro M4", result.name());
            verify(productRepository, times(1)).existsByNameIgnoreCase("iPad Pro M4");
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Thất bại: Ném DuplicateResourceException khi tên sản phẩm đã tồn tại")
        void createProduct_WithDuplicateName_ThrowsException() {
            // Given
            ProductCreateRequest request = new ProductCreateRequest();
            request.setName("MacBook Pro M3");

            when(productRepository.existsByNameIgnoreCase("MacBook Pro M3")).thenReturn(true);

            // When & Then
            assertThrows(DuplicateResourceException.class, () -> productService.createProduct(request));
            verify(productRepository, times(1)).existsByNameIgnoreCase("MacBook Pro M3");
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Kịch bản updateProduct")
    class UpdateProductTests {

        @Test
        @DisplayName("Thành công: Cập nhật sản phẩm khi ID tồn tại và tên mới hợp lệ")
        void updateProduct_WhenValid_ReturnsUpdatedResponse() {
            // Given
            ProductUpdateRequest updateRequest = new ProductUpdateRequest();
            updateRequest.setName("MacBook Pro M3 16-inch");
            updateRequest.setDescription("Mô tả mới");
            updateRequest.setPrice(2799.99);
            updateRequest.setCategory("Laptop");
            updateRequest.setStock(10);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameIgnoreCaseAndIdNot("MacBook Pro M3 16-inch", 1L)).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ProductResponse updated = productService.updateProduct(1L, updateRequest);

            // Then
            assertNotNull(updated);
            assertEquals("MacBook Pro M3 16-inch", updated.name());
            assertEquals(2799.99, updated.price());
            verify(productRepository, times(1)).save(sampleProduct);
        }

        @Test
        @DisplayName("Thất bại: Ném DuplicateResourceException khi tên mới trùng với sản phẩm khác")
        void updateProduct_WhenDuplicateNameExists_ThrowsException() {
            // Given
            ProductUpdateRequest updateRequest = new ProductUpdateRequest();
            updateRequest.setName("Dell XPS 15");

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameIgnoreCaseAndIdNot("Dell XPS 15", 1L)).thenReturn(true);

            // When & Then
            assertThrows(DuplicateResourceException.class, () -> productService.updateProduct(1L, updateRequest));
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Kịch bản deleteProduct")
    class DeleteProductTests {

        @Test
        @DisplayName("Thành công: Xóa sản phẩm khi ID tồn tại")
        void deleteProduct_WhenExists_DeletesProduct() {
            // Given
            when(productRepository.existsById(1L)).thenReturn(true);
            doNothing().when(productRepository).deleteById(1L);

            // When
            productService.deleteProduct(1L);

            // Then
            verify(productRepository, times(1)).existsById(1L);
            verify(productRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Thất bại: Ném ResourceNotFoundException khi ID sản phẩm cần xóa không tồn tại")
        void deleteProduct_WhenNotFound_ThrowsException() {
            // Given
            when(productRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(999L));
            verify(productRepository, times(1)).existsById(999L);
            verify(productRepository, never()).deleteById(anyLong());
        }
    }
}
