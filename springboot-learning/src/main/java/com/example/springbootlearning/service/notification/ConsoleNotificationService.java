package com.example.springbootlearning.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation ghi thông báo ra Console (Terminal).
 * Dùng @Qualifier("consoleNotification") khi muốn inject bean này.
 */
@Service("consoleNotification")
public class ConsoleNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationService.class);

    @Override
    public String send(String recipient, String message) {
        String result = String.format("[CONSOLE] Ghi nhận cho %s: '%s'", recipient, message);
        log.info("💻 {}", result);
        return result;
    }

    @Override
    public String getChannelName() {
        return "CONSOLE";
    }
}
