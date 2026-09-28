package com.example.springbootlearning.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minh hoạ Externalized Configuration và @Value:
 * Inject các giá trị từ file application.properties vào biến Java.
 */
@Service
public class AppInfoService {

    private static final Logger log = LoggerFactory.getLogger(AppInfoService.class);

    @Value("${app.name:Spring Boot Default}")
    private String appName;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    @Value("${app.description:Khong co mo ta}")
    private String appDescription;

    @Value("${app.max-users:50}")
    private int maxUsers;

    @Value("${app.welcome-message}")
    private String welcomeMessage;

    @Value("${app.default-channel:SMS}")
    private String defaultChannel;

    // Giá trị không tồn tại trong properties, sẽ lấy fallback sau dấu hai chấm ":"
    @Value("${app.maintenance-mode:false}")
    private boolean maintenanceMode;

    @PostConstruct
    public void printConfig() {
        log.info("==================================================");
        log.info("📋 [AppInfoService] Cấu hình nạp thành công từ properties:");
        log.info("   - Tên ứng dụng:   {}", appName);
        log.info("   - Phiên bản:      {}", appVersion);
        log.info("   - Mô tả:          {}", appDescription);
        log.info("   - Max Users:      {}", maxUsers);
        log.info("   - Welcome:        {}", welcomeMessage);
        log.info("   - Kênh mặc định:  {}", defaultChannel);
        log.info("   - Bảo trì:        {}", maintenanceMode);
        log.info("==================================================");
    }

    public Map<String, Object> getAppDetails() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("name", appName);
        details.put("version", appVersion);
        details.put("description", appDescription);
        details.put("maxUsers", maxUsers);
        details.put("welcomeMessage", welcomeMessage);
        details.put("defaultChannel", defaultChannel);
        details.put("maintenanceMode", maintenanceMode);
        return details;
    }
}
