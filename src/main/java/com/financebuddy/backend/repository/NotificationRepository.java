package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndReadStatusFalse(Long userId);

    List<Notification> findByUserIdAndNotificationType(Long userId, String notificationType);
}
