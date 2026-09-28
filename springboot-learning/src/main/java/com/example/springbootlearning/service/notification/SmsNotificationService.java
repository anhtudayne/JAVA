package com.example.springbootlearning.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Implementation gửi thông báo qua SMS.
 * Đánh dấu @Primary: Khi một class cần inject NotificationService mà KHÔNG dùng @Qualifier,
 * Spring IoC Container sẽ tự động ưu tiên chọn SmsNotificationService làm mặc định.
 */
@Service("smsNotification")
@Primary
public class SmsNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    @Override
    public String send(String recipient, String message) {
        String result = String.format("[SMS] Đã gửi tới %s: '%s'", recipient, message);
        log.info("📱 {}", result);
        return result;
    }

    @Override
    public String getChannelName() {
        return "SMS (Primary Default)";
    }
}
