package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.Notification;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final V2Store db;
    public NotificationService(V2Store db) { this.db = db; }
    public PageResult<Notification> list(long page, long size, Integer read, String type) {
        if (read != null && read != 0 && read != 1) throw new BusinessException("isRead 只能为 0 或 1");
        return db.page(Notification.class, new QueryWrapper<Notification>().eq("user_id", CurrentUserContext.require().userId())
            .eq(read != null, "is_read", read).eq(type != null, "notification_type", type).orderByDesc("create_time", "id"), page, size);
    }
    public long unread() { return db.count(Notification.class, new QueryWrapper<Notification>().eq("user_id", CurrentUserContext.require().userId()).eq("is_read", 0)); }
    public void read(Long id) {
        Long userId = CurrentUserContext.require().userId();
        if (id != null && !userId.equals(db.get(Notification.class, id).getUserId())) com.ipagency.service.impl.CaseAccessServiceImpl.denied();
        db.mapper(Notification.class).update(null, new UpdateWrapper<Notification>().eq("user_id", userId).eq(id != null, "id", id)
            .eq("is_read", 0).set("is_read", 1).set("read_time", LocalDateTime.now()));
    }
}
