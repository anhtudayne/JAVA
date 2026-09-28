package com.example.springbootlearning;

import com.example.springbootlearning.service.AppInfoService;
import com.example.springbootlearning.service.CacheService;
import com.example.springbootlearning.service.notification.EmailNotificationService;
import com.example.springbootlearning.service.notification.NotificationService;
import com.example.springbootlearning.service.notification.SmsNotificationService;
import com.example.springbootlearning.service.scope.PrototypeCounter;
import com.example.springbootlearning.service.scope.SingletonCounter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lesson03Tests {

    @Autowired
    private NotificationService defaultNotificationService; // Should be SmsNotificationService via @Primary

    @Autowired
    @Qualifier("emailNotification")
    private NotificationService emailNotificationService;

    @Autowired
    @Qualifier("consoleNotification")
    private NotificationService consoleNotificationService;

    @Autowired
    private AppInfoService appInfoService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SingletonCounter singletonCounter1;

    @Autowired
    private SingletonCounter singletonCounter2;

    @Autowired
    private ObjectProvider<PrototypeCounter> prototypeCounterProvider;

    @Autowired
    private DateTimeFormatter dateTimeFormatter;

    @Autowired
    @Qualifier("systemSignature")
    private String systemSignature;

    @Test
    @DisplayName("Kiểm tra @Primary: Mặc định inject SmsNotificationService")
    void testPrimaryInjection() {
        assertNotNull(defaultNotificationService);
        assertTrue(defaultNotificationService instanceof SmsNotificationService);
        assertTrue(defaultNotificationService.getChannelName().contains("Primary"));
    }

    @Test
    @DisplayName("Kiểm tra @Qualifier: Inject đích danh EmailNotificationService")
    void testQualifierInjection() {
        assertNotNull(emailNotificationService);
        assertTrue(emailNotificationService instanceof EmailNotificationService);
        assertEquals("EMAIL", emailNotificationService.getChannelName());
    }

    @Test
    @DisplayName("Kiểm tra @Value: Đọc giá trị chính xác từ application.properties")
    void testValueAnnotation() {
        Map<String, Object> details = appInfoService.getAppDetails();
        assertEquals("Spring Boot Learning Platform", details.get("name"));
        assertEquals("1.0.0", details.get("version"));
        assertEquals(100, details.get("maxUsers"));
        assertEquals("SMS", details.get("defaultChannel"));
    }

    @Test
    @DisplayName("Kiểm tra @PostConstruct: CacheService tự nạp 3 items khi khởi động")
    void testPostConstructLifecycle() {
        assertEquals(3, cacheService.size());
        assertEquals("RUNNING", cacheService.get("system.status"));
        assertEquals("vi_VN", cacheService.get("system.default_lang"));
    }

    @Test
    @DisplayName("Kiểm tra Bean Scope: Singleton cùng instance vs Prototype khác instance")
    void testBeanScopes() {
        // Singleton: Cùng một instance trong bộ nhớ
        assertSame(singletonCounter1, singletonCounter2);
        assertEquals(singletonCounter1.getInstanceId(), singletonCounter2.getInstanceId());

        // Prototype: Mỗi lần gọi getObject() sinh ra instance mới
        PrototypeCounter proto1 = prototypeCounterProvider.getObject();
        PrototypeCounter proto2 = prototypeCounterProvider.getObject();
        assertNotSame(proto1, proto2);
        assertNotEquals(proto1.getInstanceId(), proto2.getInstanceId());
    }

    @Test
    @DisplayName("Kiểm tra @Bean từ @Configuration: DateTimeFormatter và systemSignature")
    void testConfigurationBeans() {
        assertNotNull(dateTimeFormatter);
        assertNotNull(systemSignature);
        assertTrue(systemSignature.contains("Spring Boot 3"));
    }
}
