package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class WorkflowService {
    private final V2Store db; private final CaseAccessServiceImpl access; private final CaseStatusFacade statusFacade;
    private final BusinessEvents events; private final DomainEventPublisher publisher; private final ChecklistService checklists;
    public WorkflowService(V2Store db, CaseAccessServiceImpl access, CaseStatusFacade statusFacade, BusinessEvents events,
            DomainEventPublisher publisher, ChecklistService checklists) {
        this.db = db; this.access = access; this.statusFacade = statusFacade; this.events = events;
        this.publisher = publisher; this.checklists = checklists;
    }
    public boolean hasInstance(Long caseId) {
        try { return db.count(WorkflowInstance.class, new QueryWrapper<WorkflowInstance>().eq("case_id", caseId)) > 0; }
        catch (Exception e) { return false; }
    }
    public void startIfAbsent(Long caseId, String caseType) {
        if (hasInstance(caseId)) return;
        WorkflowDefinition def = db.one(WorkflowDefinition.class, new QueryWrapper<WorkflowDefinition>().eq("business_type", caseType).eq("status", 1));
        if (def == null) def = db.one(WorkflowDefinition.class, new QueryWrapper<WorkflowDefinition>().eq("code", "INVENTION_PATENT"));
        if (def == null) return;
        WorkflowVersion ver = db.one(WorkflowVersion.class, new QueryWrapper<WorkflowVersion>().eq("definition_id", def.getId()).eq("status", "PUBLISHED").orderByDesc("version_no").last("LIMIT 1"));
        if (ver == null) return;
        WorkflowState initial = db.one(WorkflowState.class, new QueryWrapper<WorkflowState>().eq("version_id", ver.getId()).eq("is_initial", 1));
        WorkflowInstance inst = new WorkflowInstance(); inst.setCaseId(caseId); inst.setDefinitionId(def.getId());
        inst.setVersionId(ver.getId()); inst.setCurrentState(initial == null ? "PROCESSING" : initial.getStateCode());
        db.insert(inst);
        try { checklists.instantiate(caseId, "SUBMIT_GATE"); } catch (Exception ignored) { }
    }
    public Map<String, Object> detail(Long caseId) {
        access.requireView(caseId);
        WorkflowInstance inst = db.one(WorkflowInstance.class, new QueryWrapper<WorkflowInstance>().eq("case_id", caseId));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instance", inst);
        if (inst != null) {
            result.put("transitions", db.mapper(WorkflowTransition.class).selectList(new QueryWrapper<WorkflowTransition>().eq("version_id", inst.getVersionId()).eq("from_state", inst.getCurrentState())));
            result.put("history", db.mapper(WorkflowHistory.class).selectList(new QueryWrapper<WorkflowHistory>().eq("instance_id", inst.getId()).orderByAsc("id")));
        }
        return result;
    }
    @Transactional
    public WorkflowInstance fire(Long caseId, String event, String reason) {
        access.requireManage(caseId);
        WorkflowInstance found = db.one(WorkflowInstance.class, new QueryWrapper<WorkflowInstance>().eq("case_id", caseId));
        if (found == null) throw new BusinessException("WORKFLOW_TRANSITION_DENIED: 案件未挂接流程实例", HttpStatus.CONFLICT);
        WorkflowInstance inst = db.lock(WorkflowInstance.class, found.getId());
        WorkflowTransition t = db.one(WorkflowTransition.class, new QueryWrapper<WorkflowTransition>()
            .eq("version_id", inst.getVersionId()).eq("from_state", inst.getCurrentState()).eq("event_code", event));
        if (t == null) throw new BusinessException("WORKFLOW_TRANSITION_DENIED: 当前状态不允许该事件", HttpStatus.CONFLICT);
        if (t.getAllowedRole() != null && !t.getAllowedRole().equals(CurrentUserContext.require().role()) && !CurrentUserContext.require().isAdmin())
            throw new BusinessException("WORKFLOW_TRANSITION_DENIED: 角色不允许", HttpStatus.FORBIDDEN);
        if (t.getGuardRule() != null) checklists.assertGate(caseId, t.getGuardRule());
        String from = inst.getCurrentState(); inst.setCurrentState(t.getToState()); db.update(inst);
        WorkflowHistory h = new WorkflowHistory(); h.setInstanceId(inst.getId()); h.setCaseId(caseId); h.setFromState(from);
        h.setToState(t.getToState()); h.setEventCode(event); h.setOperatorUserId(CurrentUserContext.require().userId());
        h.setReason(reason); h.setOccurredAt(LocalDateTime.now()); db.insert(h);
        CaseInfo c = statusFacade.lock(caseId);
        if (Set.of("PROCESSING", "FORMAL_EXAM", "SUBSTANTIVE_EXAM", "GRANTED", "CLOSED").contains(t.getToState()))
            statusFacade.transition(c, t.getToState());
        events.caseEvent(caseId, "CASE_STATUS", "流程推进: " + t.getToState());
        events.audit("WORKFLOW_TRANSITION", "WORKFLOW", inst.getId());
        publisher.publish("WORKFLOW_TRANSITIONED", caseId, inst.getId(), event);
        return inst;
    }
    public PageResult<WorkflowDefinition> definitions(long page, long size) { role("ADMIN"); return db.page(WorkflowDefinition.class, new QueryWrapper<WorkflowDefinition>().orderByAsc("id"), page, size); }
    @Transactional
    public WorkflowDefinition createDefinition(Map<String, Object> body) {
        role("ADMIN"); WorkflowDefinition d = new WorkflowDefinition(); d.setCode(Input.text(body, "code")); d.setName(Input.text(body, "name"));
        d.setBusinessType((String) body.getOrDefault("businessType", d.getCode())); d.setStatus(1); db.insert(d);
        WorkflowVersion v = new WorkflowVersion(); v.setDefinitionId(d.getId()); v.setVersionNo(1); v.setStatus("DRAFT"); db.insert(v);
        events.audit("CREATE_WORKFLOW", "WORKFLOW", d.getId()); return d;
    }
    @Transactional
    public WorkflowVersion newVersion(Long definitionId) {
        role("ADMIN"); WorkflowDefinition d = db.get(WorkflowDefinition.class, definitionId);
        Integer max = db.mapper(WorkflowVersion.class).selectList(new QueryWrapper<WorkflowVersion>().eq("definition_id", definitionId))
            .stream().map(WorkflowVersion::getVersionNo).max(Integer::compareTo).orElse(0);
        WorkflowVersion v = new WorkflowVersion(); v.setDefinitionId(d.getId()); v.setVersionNo(max + 1); v.setStatus("DRAFT"); db.insert(v);
        events.audit("CREATE_WORKFLOW_VERSION", "WORKFLOW", v.getId()); return v;
    }
    @Transactional
    public WorkflowVersion publish(Long versionId) {
        role("ADMIN"); WorkflowVersion v = db.lock(WorkflowVersion.class, versionId);
        if ("PUBLISHED".equals(v.getStatus())) return v;
        v.setStatus("PUBLISHED"); v.setPublishedAt(LocalDateTime.now()); db.update(v);
        events.audit("PUBLISH_WORKFLOW_VERSION", "WORKFLOW", versionId); return v;
    }
}
