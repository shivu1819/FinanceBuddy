package com.financebuddy.backend.notification;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getAll();
    long unreadCount();
    NotificationResponse markRead(Long id);
    void markAllRead();
}
