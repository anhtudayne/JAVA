package com.example.springbootlearning.controller;

import com.example.springbootlearning.service.AppInfoService;
import com.example.springbootlearning.service.CacheService;
import com.example.springbootlearning.service.notification.NotificationService;
import com.example.springbootlearning.service.scope.PrototypeCounter;
import com.example.springbootlearning.service.scope.SingletonCounter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller kiểm thử và trình diễn toàn bộ kiến thức Bài 3:
 * 1. @Primary vs @Qualifier
 * 2. @Value (qua AppInfoService)
 * 3. Bean Lifecycle (@PostConstruct / @PreDestroy qua CacheService)
 * 4. Bean Scope (Singleton vs Prototype)
 * 5. @Bean từ AppConfig (DateTimeFormatter, systemSignature)
 */
@RestController
@RequestMapping("/api/v1/lesson03")
public class Lesson03DemoController {

    // 1. Inject @Primary (mặc định chọn SmsNotificationService vì có @Primary)
    private final NotificationService defaultNotificationService;

    // 2. Inject đích danh @Qualifier("emailNotification")
    private final NotificationService emailNotificationService;

    // 3. Inject đích danh @Qualifier("consoleNotification")
    private final NotificationService consoleNotificationService;

    // 4. Inject Service sử dụng @Value
    private final AppInfoService appInfoService;

    // 5. Inject Service có @PostConstruct và @PreDestroy
    private final CacheService cacheService;

    // 6. Inject Singleton Counter
    private final SingletonCounter singletonCounter;

    // 7. Inject ObjectProvider để tạo instance Prototype mới mỗi khi gọi getObject()
    private final ObjectProvider<PrototypeCounter> prototypeCounterProvider;

    // 8. Inject Bean được tạo từ @Configuration (@Bean)
    private final DateTimeFormatter dateTimeFormatter;
    private final String systemSignature;

    public Lesson03DemoController(
            NotificationService defaultNotificationService, // Nhận @Primary (SmsNotificationService)
            @Qualifier("emailNotification") NotificationService emailNotificationService,
            @Qualifier("consoleNotification") NotificationService consoleNotificationService,
            AppInfoService appInfoService,
            CacheService cacheService,
            SingletonCounter singletonCounter,
            ObjectProvider<PrototypeCounter> prototypeCounterProvider,
            DateTimeFormatter dateTimeFormatter,
            @Qualifier("systemSignature") String systemSignature
    ) {
        this.defaultNotificationService = defaultNotificationService;
        this.emailNotificationService = emailNotificationService;
        this.consoleNotificationService = consoleNotificationService;
        this.appInfoService = appInfoService;
        this.cacheService = cacheService;
        this.singletonCounter = singletonCounter;
        this.prototypeCounterProvider = prototypeCounterProvider;
        this.dateTimeFormatter = dateTimeFormatter;
        this.systemSignature = systemSignature;
    }

    /**
     * Endpoint 1: Kiểm thử @Primary
     * URL: GET /api/v1/lesson03/notifications/default?recipient=0987654321&message=ChaoBan
     */
    @GetMapping("/notifications/default")
    public ResponseEntity<Map<String, Object>> testDefaultNotification(
            @RequestParam(defaultValue = "0901234567") String recipient,
            @RequestParam(defaultValue = "Thong bao mac dinh qua @Primary") String message
    ) {
        String logResult = defaultNotificationService.send(recipient, message);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("channel", defaultNotificationService.getChannelName());
        response.put("description", "Được inject tự động nhờ nhãn @Primary trên SmsNotificationService");
        response.put("result", logResult);
        response.put("timestamp", dateTimeFormatter.format(LocalDateTime.now()));
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 2: Kiểm thử @Qualifier("emailNotification")
     * URL: GET /api/v1/lesson03/notifications/email?recipient=test@gmail.com&message=ChaoBan
     */
    @GetMapping("/notifications/email")
    public ResponseEntity<Map<String, Object>> testEmailNotification(
            @RequestParam(defaultValue = "dev@example.com") String recipient,
            @RequestParam(defaultValue = "Email duoc gui qua @Qualifier") String message
    ) {
        String logResult = emailNotificationService.send(recipient, message);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("channel", emailNotificationService.getChannelName());
        response.put("description", "Chỉ định đích danh bằng @Qualifier(\"emailNotification\")");
        response.put("result", logResult);
        response.put("timestamp", dateTimeFormatter.format(LocalDateTime.now()));
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 3: Kiểm thử @Qualifier("consoleNotification")
     * URL: GET /api/v1/lesson03/notifications/console?recipient=SystemAdmin&message=ChaoBan
     */
    @GetMapping("/notifications/console")
    public ResponseEntity<Map<String, Object>> testConsoleNotification(
            @RequestParam(defaultValue = "Admin") String recipient,
            @RequestParam(defaultValue = "Log console qua @Qualifier") String message
    ) {
        String logResult = consoleNotificationService.send(recipient, message);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("channel", consoleNotificationService.getChannelName());
        response.put("description", "Chỉ định đích danh bằng @Qualifier(\"consoleNotification\")");
        response.put("result", logResult);
        response.put("timestamp", dateTimeFormatter.format(LocalDateTime.now()));
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 4: Kiểm thử @Value (Externalized Configuration)
     * URL: GET /api/v1/lesson03/config-info
     */
    @GetMapping("/config-info")
    public ResponseEntity<Map<String, Object>> getAppConfigInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("source", "application.properties thông qua @Value");
        response.put("configData", appInfoService.getAppDetails());
        response.put("signature", systemSignature);
        response.put("currentTime", dateTimeFormatter.format(LocalDateTime.now()));
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 5: Kiểm thử Bean Lifecycle (@PostConstruct / @PreDestroy qua CacheService)
     * URL: GET /api/v1/lesson03/cache
     */
    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Dữ liệu được nạp tự động lúc app khởi động qua @PostConstruct");
        response.put("cacheSize", cacheService.size());
        response.put("cacheEntries", cacheService.getAll());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 6: Kiểm thử Bean Scope (Singleton vs Prototype)
     * URL: GET /api/v1/lesson03/scope-demo
     * Gọi nhiều lần endpoint này để thấy:
     * - SingletonCounter giữ nguyên instance ID và giá trị tăng dần liên tục (+1 mỗi request).
     * - PrototypeCounter sinh instance ID mới mỗi lần gọi và giá trị luôn bắt đầu lại từ 1.
     */
    @GetMapping("/scope-demo")
    public ResponseEntity<Map<String, Object>> testScopeDemo() {
        // Singleton: Cùng 1 instance được dùng chung, đếm tăng dần
        int sCount = singletonCounter.incrementAndGet();
        String sId = singletonCounter.getInstanceId();

        // Prototype: Yêu cầu instance mới từ ObjectProvider
        PrototypeCounter prototypeCounter = prototypeCounterProvider.getObject();
        int pCount = prototypeCounter.incrementAndGet();
        String pId = prototypeCounter.getInstanceId();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("explanation", "Gọi endpoint này nhiều lần liên tiếp để thấy sự khác biệt!");

        Map<String, Object> singletonInfo = new LinkedHashMap<>();
        singletonInfo.put("scope", "Singleton (Mặc định)");
        singletonInfo.put("instanceId", sId);
        singletonInfo.put("currentCount", sCount);
        singletonInfo.put("behavior", "Instance ID không đổi, count tăng liên tục qua các request");
        response.put("singleton", singletonInfo);

        Map<String, Object> prototypeInfo = new LinkedHashMap<>();
        prototypeInfo.put("scope", "Prototype (@Scope(\"prototype\"))");
        prototypeInfo.put("instanceId", pId);
        prototypeInfo.put("currentCount", pCount);
        prototypeInfo.put("behavior", "Instance ID luôn thay đổi (đối tượng mới trong RAM), count luôn = 1");
        response.put("prototype", prototypeInfo);

        return ResponseEntity.ok(response);
    }
}
