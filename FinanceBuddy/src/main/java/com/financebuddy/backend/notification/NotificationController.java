package com.financebuddy.backend.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;
    @GetMapping public List<NotificationResponse> getAll(){return service.getAll();}
    @GetMapping("/unread-count") public Map<String,Long> unreadCount(){return Map.of("count",service.unreadCount());}
    @PatchMapping("/{id}/read") public NotificationResponse markRead(@PathVariable Long id){return service.markRead(id);}
    @PatchMapping("/read-all") public Map<String,String> markAllRead(){service.markAllRead();return Map.of("message","Notifications marked as read.");}
}
