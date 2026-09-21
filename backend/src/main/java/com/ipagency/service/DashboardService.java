package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final V2Store db; private final CaseAccessServiceImpl access; private final NotificationService notifications;
    public DashboardService(V2Store db, CaseAccessServiceImpl access, NotificationService notifications) { this.db = db; this.access = access; this.notifications = notifications; }
    private QueryWrapper<CaseInfo> cases() { return access.scope(new QueryWrapper<CaseInfo>(), "id", true); }
    private QueryWrapper<DeadlineTask> deadlines() {
        var q = access.scope(new QueryWrapper<DeadlineTask>(), "case_id", false);
        if ("AGENT".equals(CurrentUserContext.require().role())) q.eq("agent_id", access.agent().getId());
        return q;
    }
    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cases", db.count(CaseInfo.class, cases()));
        result.put("processingCases", db.count(CaseInfo.class, cases().in("status", "PROCESSING", "FORMAL_EXAM", "SUBSTANTIVE_EXAM", "PRELIMINARY_PASSED", "REEXAMINATION")));
        result.put("pendingDeadlines", db.count(DeadlineTask.class, deadlines().ne("status", "COMPLETED")));
        result.put("upcomingDeadlines", db.count(DeadlineTask.class, deadlines().ne("status", "COMPLETED").le("official_deadline", LocalDateTime.now().plusDays(7))));
        result.put("unreadNotifications", notifications.unread());
        result.put("monthlyCompletedCases", db.count(CaseInfo.class, cases().eq("status", "CLOSED").ge("update_time", LocalDate.now().withDayOfMonth(1).atStartOfDay())));
        if (CurrentUserContext.require().isAdmin()) {
            result.put("pendingReview", db.count(CaseInfo.class, cases().eq("status", "PENDING_REVIEW")));
            result.put("pendingAssignment", db.count(CaseInfo.class, cases().eq("status", "PENDING_ASSIGNMENT")));
            result.put("monthlyBillAmount", db.mapper(FeeBill.class).selectObjs(new QueryWrapper<FeeBill>().select("COALESCE(SUM(payable_amount),0)")
                .ge("create_time", LocalDate.now().withDayOfMonth(1).atStartOfDay()).notIn("status", "CANCELLED", "REFUNDED")).get(0));
        } else if ("CLIENT".equals(CurrentUserContext.require().role())) result.put("pendingBills", db.count(FeeBill.class,
            new QueryWrapper<FeeBill>().eq("client_id", access.client().getId()).in("status", "PENDING_CONFIRM", "PENDING_PAYMENT")));
        return result;
    }
    public Map<String, Object> statistics() {
        Map<String, Object> result = new LinkedHashMap<>(overview());
        result.put("casesByStatus", db.mapper(CaseInfo.class).selectMaps(cases().select("status", "COUNT(*) AS total").groupBy("status")));
        result.put("casesByType", db.mapper(CaseInfo.class).selectMaps(cases().select("case_type AS caseType", "COUNT(*) AS total").groupBy("case_type")));
        result.put("deadlinesByStatus", db.mapper(DeadlineTask.class).selectMaps(deadlines().select("status", "COUNT(*) AS total").groupBy("status")));
        return result;
    }
}
