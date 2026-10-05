package com.financebuddy.backend.notification;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String notificationType;
    private Boolean readStatus;
    private LocalDateTime createdAt;
}
