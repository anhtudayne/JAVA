package com.example.springbootlearning.config;

import com.example.springbootlearning.client.PostApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

/**
 * 📘 BÀI 16 — Cấu hình RestClient & HTTP Service Client Bean
 *
 * Cấu hình:
 *   - Base URL của External Service (mặc định trỏ đến JSONPlaceholder)
 *   - Thời gian chờ: connectTimeout (5s) và readTimeout (10s)
 *   - HTTP Headers mặc định: Content-Type, Accept, User-Agent
 *   - Request Interceptor ghi log lời gọi API
 *   - Đăng ký Dynamic Proxy Bean cho PostApiClient qua HttpServiceProxyFactory
 */
@Configuration
public class ExternalApiClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiClientConfig.class);

    @Value("${app.external.jsonplaceholder.base-url:https://jsonplaceholder.typicode.com}")
    private String jsonPlaceholderBaseUrl;

    /**
     * Bean RestClient nền tảng:
     * Cung cấp Fluent API cho các tác vụ cần tùy biến sâu hoặc kiểm soát luồng đồng bộ.
     */
    @Bean
    public RestClient jsonPlaceholderRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .baseUrl(jsonPlaceholderBaseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("User-Agent", "SpringBoot-Learning-Client/1.0")
                .requestInterceptor((request, body, execution) -> {
                    log.info("🌐 [External Call] {} {}", request.getMethod(), request.getURI());
                    return execution.execute(request, body);
                })
                .build();
    }

    /**
     * Bean Declarative Client (HTTP Interface):
     * Spring tự động sinh Proxy instance cài đặt interface PostApiClient.
     */
    @Bean
    public PostApiClient postApiClient(RestClient jsonPlaceholderRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(jsonPlaceholderRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(PostApiClient.class);
    }
}
