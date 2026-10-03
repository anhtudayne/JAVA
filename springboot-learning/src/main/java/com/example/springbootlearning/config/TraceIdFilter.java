package com.example.springbootlearning.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 📘 BÀI 7 — Servlet Filter gắn TraceId cho mỗi HTTP request.
 *
 * Mục đích:
 *   Khi 100 người dùng gọi API cùng lúc, log của họ xen kẽ nhau.
 *   TraceId giúp nhóm tất cả dòng log của 1 request lại với nhau.
 *   → grep "abc-1234" logs/app.log → ra TOÀN BỘ log của request đó.
 *
 * Cơ chế:
 *   ① Mỗi request đến → tạo 1 traceId ngẫu nhiên (UUID 8 ký tự)
 *   ② Gắn traceId vào MDC (Mapped Diagnostic Context) — ThreadLocal map
 *   ③ Từ giờ mọi log.xxx() trong cùng thread đều tự động có traceId
 *   ④ Khi request kết thúc → XÓA traceId khỏi MDC (tránh rò rỉ sang thread khác)
 *
 * Logback pattern lấy traceId qua: %X{traceId}
 *
 * @Order(1) → Chạy đầu tiên trong chuỗi filter, trước mọi filter khác
 */
@Component
@Order(1)
public class TraceIdFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            // ① Tạo traceId ngẫu nhiên — lấy 8 ký tự đầu của UUID cho ngắn gọn
            String traceId = UUID.randomUUID().toString().substring(0, 8);

            // ② Gắn vào MDC — từ giờ mọi log trong thread này đều có traceId
            MDC.put(TRACE_ID_KEY, traceId);

            // ③ Log request đầu vào
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            log.info("📥 {} {} [traceId={}]",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    traceId);

            // ④ Chuyển tiếp request cho filter/controller tiếp theo
            chain.doFilter(request, response);

            // ⑤ Log response status (sau khi Controller xử lý xong)
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            log.info("📤 {} {} → {} [traceId={}]",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    traceId);

        } finally {
            // ⑥ QUAN TRỌNG: Xóa MDC sau khi request xong!
            //
            // Tomcat dùng Thread Pool — thread xử lý xong request A
            // có thể được tái sử dụng cho request B.
            // Nếu không xóa → request B sẽ "thừa kế" traceId của A → log bị sai!
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
