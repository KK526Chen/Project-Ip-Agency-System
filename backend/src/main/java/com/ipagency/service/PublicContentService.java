package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import java.time.*;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class PublicContentService {
    private final V2Store db; private final Input input; private final BusinessEvents events;
    public PublicContentService(V2Store db, Input input, BusinessEvents events) { this.db = db; this.input = input; this.events = events; }
    public Class<?> type(String resource) {
        return switch (resource) { case "services", "service-products" -> ServiceProduct.class; case "success-cases" -> SuccessCase.class;
            case "announcements" -> Announcement.class; default -> throw new BusinessException("内容类型不存在", org.springframework.http.HttpStatus.NOT_FOUND); };
    }
    private <T> QueryWrapper<T> query(Class<T> type, boolean admin, String keyword, String category) {
        QueryWrapper<T> q = new QueryWrapper<>();
        if (!admin) {
            if (type == SuccessCase.class) q.eq("publish_status", 1); else q.eq("status", 1);
            if (type == Announcement.class) q.eq("target_scope", "ALL").le("publish_time", LocalDateTime.now())
                .and(w -> w.isNull("start_date").or().le("start_date", LocalDate.now()))
                .and(w -> w.isNull("end_date").or().ge("end_date", LocalDate.now()));
        }
        String name = type == ServiceProduct.class ? "service_name" : type == SuccessCase.class ? "case_name" : "title";
        q.like(keyword != null && !keyword.isBlank(), name, keyword)
            .eq(category != null, type == Announcement.class ? "announcement_type" : "service_type", category);
        if (type == Announcement.class) q.orderByDesc("is_top", "publish_time");
        return q.orderByDesc("id");
    }
    public <T> PageResult<T> list(Class<T> type, boolean admin, long page, long size, String keyword, String category) {
        if (admin) role("ADMIN"); return db.page(type, query(type, admin, keyword, category), page, size);
    }
    public <T> T detail(Class<T> type, Long id) {
        T result = db.one(type, query(type, false, null, null).eq("id", id));
        if (result == null) throw new BusinessException("内容不存在", org.springframework.http.HttpStatus.NOT_FOUND); return result;
    }
    public <T> T adminDetail(Class<T> type, Long id) {
        role("ADMIN");
        return db.get(type, id);
    }
    @Transactional
    public <T> T save(Class<T> type, Long id, Map<String, Object> body) {
        role("ADMIN"); T value;
        try { value = id == null ? type.getDeclaredConstructor().newInstance() : db.get(type, id); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        String fields = type == ServiceProduct.class ? "serviceNo serviceName serviceType targetType description processDesc officialFee agencyFee estimatedCycle requiredMaterials advantages status"
            : type == SuccessCase.class ? "caseNo caseName serviceType clientIndustry technicalField highlights result grantDate description publishStatus"
            : "announcementNo title announcementType targetScope content publishTime startDate endDate isTop status";
        input.apply(body, value, fields);
        if (value instanceof Announcement a) {
            a.setPublisherUserId(CurrentUserContext.require().userId());
            if (a.getPublishTime() == null) a.setPublishTime(LocalDateTime.now());
            if (a.getStartDate() != null && a.getEndDate() != null && a.getEndDate().isBefore(a.getStartDate())) throw new BusinessException("公告日期范围无效");
        }
        if (id == null) db.insert(value); else db.update(value);
        events.audit("SAVE_CONTENT", type.getSimpleName(), id); return value;
    }
    @Transactional public <T> void delete(Class<T> type, Long id) {
        role("ADMIN"); db.get(type, id); db.mapper(type).deleteById(id); events.audit("DELETE_CONTENT", type.getSimpleName(), id);
    }
}
