package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;
    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize,
            @RequestParam(required=false) Integer isRead,
            @RequestParam(required=false) String notificationType) {
        return ApiResponse.success(notifications.list(pageNum,pageSize,isRead,notificationType));
    }

    @GetMapping("/unread-count")
    public ApiResponse<?> unread() {
        return ApiResponse.success(notifications.unread());
    }

    @PutMapping("/{id}/read")
    public ApiResponse<?> read(@PathVariable Long id) {
        notifications.read(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<?> all() {
        notifications.read(null);
        return ApiResponse.success(null);
    }
}
