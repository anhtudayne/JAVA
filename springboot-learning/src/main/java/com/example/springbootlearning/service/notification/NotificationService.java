package com.example.springbootlearning.service.notification;

/**
 * Interface đại diện cho dịch vụ gửi thông báo.
 * Dùng để minh hoạ bài toán: 1 Interface có nhiều Implementation (@Primary, @Qualifier).
 */
public interface NotificationService {
    String send(String recipient, String message);
    String getChannelName();
}
