package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.entity.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bounded batches; a task receives one reminder per day, including overdue tasks. */
@Service
@ConditionalOnProperty(name = "app.reminders.enabled", havingValue = "true", matchIfMissing = true)
public class DeadlineReminderService {
    private final V2Store db; private final BusinessEvents events;
    public DeadlineReminderService(V2Store db, BusinessEvents events) { this.db = db; this.events = events; }
    @Scheduled(initialDelayString = "${app.reminders.initial-delay-ms:60000}", fixedDelayString = "${app.reminders.interval-ms:300000}")
    @Transactional
    public void remind() {
        var due = db.page(DeadlineTask.class, new QueryWrapper<DeadlineTask>().ne("status", "COMPLETED")
            .le("official_deadline", LocalDateTime.now().plusDays(7))
            .notExists("SELECT 1 FROM notification n WHERE n.business_type='DEADLINE_REMINDER' AND n.business_id=deadline_task.id AND n.is_deleted=0 AND n.create_time >= CURRENT_DATE")
            .orderByAsc("official_deadline", "id"), 1, 100);
        for (DeadlineTask candidate : due.list()) {
            DeadlineTask task = db.lock(DeadlineTask.class, candidate.getId());
            if ("COMPLETED".equals(task.getStatus())) continue;
            if (db.count(Notification.class, new QueryWrapper<Notification>().eq("business_type", "DEADLINE_REMINDER").eq("business_id", task.getId())
                .ge("create_time", java.time.LocalDate.now().atStartOfDay())) > 0) continue;
            CaseInfo c = db.get(CaseInfo.class, task.getCaseId());
            LinkedHashSet<Long> recipients = new LinkedHashSet<>();
            recipients.add(db.get(ClientProfile.class, c.getClientId()).getUserId());
            if (task.getAgentId() != null) recipients.add(db.get(AgentProfile.class, task.getAgentId()).getUserId());
            if (task.getOfficialDeadline().isBefore(LocalDateTime.now())) {
                task.setStatus("OVERDUE"); db.update(task);
                try {
                    RiskRecord r = new RiskRecord(); r.setCaseId(task.getCaseId()); r.setRiskType("DEADLINE"); r.setLevelCode("HIGH");
                    r.setTitle("时限逾期: " + task.getTaskName()); r.setStatus("OPEN"); r.setRelatedId(task.getId()); db.insert(r);
                } catch (Exception ignored) { }
            }
            for (Long userId : recipients) events.notifyUser(userId, "DEADLINE", "时限提醒: " + task.getTaskName(), "DEADLINE_REMINDER", task.getId());
        }
    }
}
