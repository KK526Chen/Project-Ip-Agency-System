package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeadlineService {
    private final V2Store db; private final CaseAccessServiceImpl access; private final Input input; private final BusinessEvents events;
    private final DomainEventPublisher publisher;
    public DeadlineService(V2Store db, CaseAccessServiceImpl access, Input input, BusinessEvents events, DomainEventPublisher publisher) {
        this.db = db; this.access = access; this.input = input; this.events = events; this.publisher = publisher;
    }
    public PageResult<DeadlineTask> list(Long caseId, String status, boolean upcoming, long page, long size) {
        if (caseId != null) access.requireView(caseId);
        var q = access.scope(new QueryWrapper<DeadlineTask>(), "case_id", false).eq(caseId != null, "case_id", caseId).eq(status != null, "status", status);
        if ("AGENT".equals(CurrentUserContext.require().role())) q.eq("agent_id", access.agent().getId());
        if (upcoming) q.ne("status", "COMPLETED").le("official_deadline", LocalDateTime.now().plusDays(7));
        return db.page(DeadlineTask.class, q.orderByAsc("official_deadline", "id"), page, size);
    }
    @Transactional
    public DeadlineTask save(Long id, Map<String, Object> body) {
        DeadlineTask d = id == null ? new DeadlineTask() : db.lock(DeadlineTask.class, id);
        if (id == null) { d.setCaseId(Input.id(body, "caseId")); d.setStatus("NOT_STARTED"); d.setPriority("MEDIUM"); }
        access.requireManage(d.getCaseId());
        if (id != null && "AGENT".equals(CurrentUserContext.require().role())
                && !Objects.equals(access.agent().getId(), d.getAgentId())) CaseAccessServiceImpl.denied();
        if (id != null) CaseWorkflowService.state(d.getStatus(), "NOT_STARTED", "IN_PROGRESS", "OVERDUE");
        Map<String, Object> fields = new LinkedHashMap<>(body);
        if (fields.containsKey("caseId") && !Objects.equals(Input.id(fields, "caseId"), d.getCaseId())) throw new BusinessException("不能更换任务所属案件");
        fields.remove("caseId");
        input.apply(fields, d, "agentId documentId deadlineType taskName officialDeadline internalDeadline priority remindType description status");
        CaseWorkflowService.state(d.getStatus(), "NOT_STARTED", "IN_PROGRESS", "OVERDUE");
        if (d.getAgentId() == null) d.setAgentId(db.get(CaseInfo.class, d.getCaseId()).getPrincipalAgentId());
        if (d.getAgentId() == null || !access.assigned(d.getCaseId(), d.getAgentId())) throw new BusinessException("任务代理人必须是案件当前代理人");
        if ("AGENT".equals(CurrentUserContext.require().role()) && !Objects.equals(access.agent().getId(), d.getAgentId())) CaseAccessServiceImpl.denied();
        if (d.getDocumentId() != null && !Objects.equals(db.get(CaseDocument.class, d.getDocumentId()).getCaseId(), d.getCaseId())) throw new BusinessException("文档不属于该案件");
        if (d.getOfficialDeadline() != null && d.getInternalDeadline() != null && d.getInternalDeadline().isAfter(d.getOfficialDeadline())) throw new BusinessException("内部期限不能晚于官方期限");
        if (id == null) db.insert(d); else db.update(d);
        events.caseEvent(d.getCaseId(), "DEADLINE", "时限任务: " + d.getTaskName()); events.audit("SAVE_DEADLINE", "DEADLINE", d.getId());
        publisher.publish(id == null ? "DEADLINE_CREATED" : "DEADLINE_CHANGED", d.getCaseId(), d.getId(), d.getTaskName());
        return d;
    }
    @Transactional
    public DeadlineTask complete(Long id) {
        DeadlineTask d = db.lock(DeadlineTask.class, id); access.requireManage(d.getCaseId());
        if ("AGENT".equals(CurrentUserContext.require().role()) && !Objects.equals(access.agent().getId(), d.getAgentId())) CaseAccessServiceImpl.denied();
        if ("COMPLETED".equals(d.getStatus())) throw new BusinessException("DEADLINE_ALREADY_COMPLETED: 任务已完成", org.springframework.http.HttpStatus.CONFLICT);
        CaseWorkflowService.state(d.getStatus(), "NOT_STARTED", "IN_PROGRESS", "OVERDUE");
        d.setStatus("COMPLETED"); d.setCompletedTime(LocalDateTime.now()); db.update(d);
        events.audit("COMPLETE_DEADLINE", "DEADLINE", id);
        publisher.publish("DEADLINE_COMPLETED", d.getCaseId(), id, d.getTaskName());
        return d;
    }
}
