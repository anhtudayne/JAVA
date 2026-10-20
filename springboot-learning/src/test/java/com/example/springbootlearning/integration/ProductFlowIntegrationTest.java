package com.example.springbootlearning.integration;

import com.example.springbootlearning.dto.request.ProductCreateRequest;
import com.example.springbootlearning.dto.request.ProductUpdateRequest;
import com.example.springbootlearning.model.Product;
import com.example.springbootlearning.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 📘 BÀI 15: Full Integration Test với @SpringBootTest(webEnvironment = RANDOM_PORT)
 *
 * Kiểm thử toàn diện từ HTTP Layer qua TCP Network Socket thật -> Embedded Tomcat ->
 * DispatcherServlet -> Controller -> Service -> Repository -> Database.
 * Sử dụng JDK HttpClient chuẩn mực để gửi HTTP Request độc lập như một Client thực tế.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Product Flow - Full Integration Test với @SpringBootTest")
class ProductFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpClient httpClient;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        baseUrl = "http://localhost:" + port + "/api/v1/products";
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Luồng E2E: Tạo mới sản phẩm qua POST và kiểm tra lại qua GET")
    void testCreateAndGetProductFlow() throws Exception {
        // 1. Chuẩn bị request body JSON
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Samsung Galaxy S24 Ultra");
        request.setDescription("Smartphone cao cấp trang bị Galaxy AI");
        request.setPrice(1299.0);
        request.setCategory("Smartphone");
        request.setStock(20);

        String jsonBody = objectMapper.writeValueAsString(request);

        // Gửi HTTP POST
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        // Kiểm tra HTTP Status 201 Created và header Location
        assertThat(postResponse.statusCode()).isEqualTo(201);
        assertThat(postResponse.headers().firstValue("Location")).isPresent();

        JsonNode postJson = objectMapper.readTree(postResponse.body());
        assertThat(postJson.get("status").asInt()).isEqualTo(201);
        long productId = postJson.get("data").get("id").asLong();
        assertThat(postJson.get("data").get("name").asText()).isEqualTo("Samsung Galaxy S24 Ultra");
        assertThat(postJson.get("data").get("price").asDouble()).isEqualTo(1299.0);

        // 2. Gửi HTTP GET để kiểm tra sản phẩm đã lưu vào DB
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + productId))
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(getResponse.statusCode()).isEqualTo(200);

        JsonNode getJson = objectMapper.readTree(getResponse.body());
        assertThat(getJson.get("data").get("name").asText()).isEqualTo("Samsung Galaxy S24 Ultra");
        assertThat(getJson.get("data").get("stock").asInt()).isEqualTo(20);
    }

    @Test
    @DisplayName("Validation Error: Gửi request thiếu tên sản phẩm và giá âm trả về HTTP 400")
    void testCreateProduct_ValidationError() throws Exception {
        ProductCreateRequest invalidRequest = new ProductCreateRequest();
        invalidRequest.setName(""); // Vi phạm @NotBlank
        invalidRequest.setPrice(-50.0); // Vi phạm @Positive
        invalidRequest.setCategory("Laptop");
        invalidRequest.setStock(10);

        String jsonBody = objectMapper.writeValueAsString(invalidRequest);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        // Phải trả về HTTP 400 Bad Request kèm thông điệp lỗi validation
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Giá sản phẩm phải lớn hơn 0");
        assertThat(response.body()).contains("Dữ liệu không hợp lệ");
    }

    @Test
    @DisplayName("Luồng Cập Nhật: PUT cập nhật sản phẩm thành công")
    void testUpdateProductFlow() throws Exception {
        // 1. Tạo sản phẩm ban đầu trong DB
        Product product = new Product(null, "Dell Inspiron 15", "Laptop văn phòng", 750.0, "Laptop", 15);
        Product saved = productRepository.save(product);

        // 2. Gửi HTTP PUT cập nhật
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Dell Inspiron 15 (Upgraded)");
        updateRequest.setDescription("Laptop văn phòng nâng cấp RAM 16GB");
        updateRequest.setPrice(820.0);
        updateRequest.setCategory("Laptop");
        updateRequest.setStock(12);

        String jsonBody = objectMapper.writeValueAsString(updateRequest);

        HttpRequest putRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + saved.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> updateResponse = httpClient.send(putRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(updateResponse.statusCode()).isEqualTo(200);

        JsonNode updateJson = objectMapper.readTree(updateResponse.body());
        assertThat(updateJson.get("data").get("name").asText()).isEqualTo("Dell Inspiron 15 (Upgraded)");
        assertThat(updateJson.get("data").get("price").asDouble()).isEqualTo(820.0);
    }

    @Test
    @DisplayName("Luồng Xóa: DELETE sản phẩm trả về 204 và truy vấn lại trả về 404")
    void testDeleteProductFlow() throws Exception {
        // 1. Tạo sản phẩm để chuẩn bị xóa
        Product product = new Product(null, "Sản phẩm cần xóa", "Tạm thời", 100.0, "Test", 1);
        Product saved = productRepository.save(product);

        // 2. Gửi DELETE
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + saved.getId()))
                .DELETE()
                .build();

        HttpResponse<Void> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.discarding());
        assertThat(deleteResponse.statusCode()).isEqualTo(204);

        // 3. GET lại -> Phải trả về 404 NOT FOUND
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + saved.getId()))
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(getResponse.statusCode()).isEqualTo(404);
    }
}
