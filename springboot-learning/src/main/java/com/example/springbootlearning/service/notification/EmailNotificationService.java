package com.example.springbootlearning.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation gửi thông báo qua Email.
 * Tên bean mặc định là "emailNotificationService", có thể đặt tên ngắn là "emailNotification".
 */
@Service("emailNotification")
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Override
    public String send(String recipient, String message) {
        String result = String.format("[EMAIL] Đã gửi tới %s: '%s'", recipient, message);
        log.info("📧 {}", result);
        return result;
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }
}
