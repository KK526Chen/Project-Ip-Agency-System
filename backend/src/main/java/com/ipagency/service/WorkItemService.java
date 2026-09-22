package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class WorkItemService {
    private static final Set<String> OPEN = Set.of("TODO", "IN_PROGRESS", "BLOCKED", "REVIEW");
    private final V2Store db; private final CaseAccessServiceImpl access; private final Input input;
    private final BusinessEvents events; private final DomainEventPublisher publisher;
    public WorkItemService(V2Store db, CaseAccessServiceImpl access, Input input, BusinessEvents events, DomainEventPublisher publisher) {
        this.db = db; this.access = access; this.input = input; this.events = events; this.publisher = publisher;
    }
    public PageResult<WorkItem> list(Long caseId, String status, long page, long size) {
        if (caseId != null) access.requireView(caseId);
        var q = access.scope(new QueryWrapper<WorkItem>(), "case_id", false);
        q.eq(caseId != null, "case_id", caseId).eq(status != null, "status", status).orderByDesc("id");
        if ("AGENT".equals(CurrentUserContext.require().role())) q.eq("assignee_agent_id", access.agent().getId());
        return db.page(WorkItem.class, q, page, size);
    }
    @Transactional
    public WorkItem save(Long id, Map<String, Object> body) {
        WorkItem w = id == null ? new WorkItem() : db.lock(WorkItem.class, id);
        if (id == null) {
            w.setCaseId(Input.id(body, "caseId")); w.setStatus("TODO"); w.setPriority("MEDIUM");
            w.setTransferPolicy("FOLLOW_PRINCIPAL"); w.setCreatorUserId(CurrentUserContext.require().userId());
        }
        access.requireManage(w.getCaseId());
        Map<String, Object> fields = new LinkedHashMap<>(body); fields.remove("caseId");
        input.apply(fields, w, "stageId deadlineId parentId workType title description assigneeAgentId priority dueTime estimatedHours transferPolicy");
        if (w.getAssigneeAgentId() == null) w.setAssigneeAgentId(db.get(CaseInfo.class, w.getCaseId()).getPrincipalAgentId());
        if (id == null) db.insert(w); else db.update(w);
        events.audit(id == null ? "CREATE_WORK_ITEM" : "UPDATE_WORK_ITEM", "WORK_ITEM", w.getId());
        publisher.publish("WORK_ITEM_CREATED", w.getCaseId(), w.getId(), w.getTitle());
        return w;
    }
    @Transactional
    public WorkItem start(Long id, boolean override, String reason) {
        WorkItem w = db.lock(WorkItem.class, id); access.requireManage(w.getCaseId());
        CaseWorkflowService.state(w.getStatus(), "TODO", "BLOCKED");
        if (!override && blocked(w)) throw new BusinessException("WORK_DEPENDENCY_BLOCKED: 前置任务未完成", HttpStatus.CONFLICT);
        if (override) {
            role("ADMIN");
            if (reason == null || reason.isBlank()) throw new BusinessException("覆盖 DAG 必须填写原因");
            events.audit("WORK_DEPENDENCY_OVERRIDE", "WORK_ITEM", id);
        }
        w.setStatus("IN_PROGRESS"); w.setStartTime(LocalDateTime.now()); db.update(w);
        events.audit("START_WORK_ITEM", "WORK_ITEM", id); return w;
    }
    @Transactional
    public WorkItem complete(Long id) {
        WorkItem w = db.lock(WorkItem.class, id); access.requireManage(w.getCaseId());
        CaseWorkflowService.state(w.getStatus(), "TODO", "IN_PROGRESS", "REVIEW", "BLOCKED");
        BigDecimal hours = db.mapper(Timesheet.class).selectList(new QueryWrapper<Timesheet>().eq("work_item_id", id))
            .stream().map(Timesheet::getHours).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        w.setActualHours(hours); w.setStatus("DONE"); w.setCompletedTime(LocalDateTime.now()); db.update(w);
        events.audit("COMPLETE_WORK_ITEM", "WORK_ITEM", id);
        publisher.publish("WORK_ITEM_COMPLETED", w.getCaseId(), id, w.getTitle());
        return w;
    }
    @Transactional
    public WorkDependency addDependency(Long successorId, Long predecessorId, String type) {
        WorkItem succ = db.lock(WorkItem.class, successorId); WorkItem pred = db.get(WorkItem.class, predecessorId);
        access.requireManage(succ.getCaseId());
        if (!Objects.equals(succ.getCaseId(), pred.getCaseId())) throw new BusinessException("依赖任务必须属于同一案件");
        if (Objects.equals(predecessorId, successorId)) throw new BusinessException("WORK_DEPENDENCY_CYCLE: 不能依赖自身");
        String kind = type == null || type.isBlank() ? "FINISH_TO_START" : type;
        List<WorkDependency> existing = db.mapper(WorkDependency.class).selectList(new QueryWrapper<WorkDependency>().eq("case_id", succ.getCaseId()));
        Map<Long, List<Long>> graph = new HashMap<>();
        for (WorkDependency d : existing) graph.computeIfAbsent(d.getSuccessorId(), k -> new ArrayList<>()).add(d.getPredecessorId());
        graph.computeIfAbsent(successorId, k -> new ArrayList<>()).add(predecessorId);
        if (createsCycle(graph)) throw new BusinessException("WORK_DEPENDENCY_CYCLE: 依赖形成环", HttpStatus.CONFLICT);
        WorkDependency dep = new WorkDependency(); dep.setCaseId(succ.getCaseId()); dep.setPredecessorId(predecessorId);
        dep.setSuccessorId(successorId); dep.setDependencyType(kind); db.insert(dep);
        events.audit("ADD_WORK_DEPENDENCY", "WORK_ITEM", successorId); return dep;
    }
    public static boolean createsCycle(Map<Long, List<Long>> graph) {
        Set<Long> visiting = new HashSet<>(), visited = new HashSet<>();
        for (Long node : graph.keySet()) if (dfs(node, graph, visiting, visited)) return true;
        return false;
    }
    private static boolean dfs(Long node, Map<Long, List<Long>> graph, Set<Long> visiting, Set<Long> visited) {
        if (visiting.contains(node)) return true;
        if (visited.contains(node)) return false;
        visiting.add(node);
        for (Long next : graph.getOrDefault(node, List.of())) if (dfs(next, graph, visiting, visited)) return true;
        visiting.remove(node); visited.add(node); return false;
    }
    private boolean blocked(WorkItem w) {
        List<WorkDependency> deps = db.mapper(WorkDependency.class).selectList(new QueryWrapper<WorkDependency>()
            .eq("successor_id", w.getId()).eq("dependency_type", "FINISH_TO_START"));
        for (WorkDependency d : deps) {
            WorkItem pred = db.get(WorkItem.class, d.getPredecessorId());
            if (!"DONE".equals(pred.getStatus())) return true;
        }
        return false;
    }
    public void transferFollowPrincipal(Long caseId, Long oldAgent, Long newAgent) {
        db.mapper(WorkItem.class).update(null, new UpdateWrapper<WorkItem>().eq("case_id", caseId)
            .eq("transfer_policy", "FOLLOW_PRINCIPAL").notIn("status", "DONE", "CANCELLED")
            .and(q -> q.isNull("assignee_agent_id").or().eq(oldAgent != null, "assignee_agent_id", oldAgent))
            .set("assignee_agent_id", newAgent));
    }
    @Transactional
    public Timesheet addTimesheet(Map<String, Object> body) {
        Long itemId = Input.id(body, "workItemId");
        WorkItem w = db.get(WorkItem.class, itemId); access.requireView(w.getCaseId());
        Timesheet t = new Timesheet(); t.setWorkItemId(itemId); t.setCaseId(w.getCaseId());
        Map<String, Object> fields = new LinkedHashMap<>(body); fields.remove("workItemId");
        input.apply(fields, t, "workDate hours description");
        if (t.getHours() == null || t.getHours().compareTo(BigDecimal.ZERO) <= 0 || t.getHours().compareTo(new BigDecimal("24")) > 0)
            throw new BusinessException("工时必须大于 0 且不超过 24");
        Long agentId;
        if ("AGENT".equals(CurrentUserContext.require().role())) {
            agentId = access.agent().getId();
            if (!Objects.equals(agentId, w.getAssigneeAgentId()) && !CurrentUserContext.require().isAdmin())
                CaseAccessServiceImpl.denied();
        } else { role("ADMIN"); agentId = w.getAssigneeAgentId(); }
        t.setAgentId(agentId);
        if (t.getWorkDate() == null) t.setWorkDate(LocalDate.now());
        db.insert(t); events.audit("SAVE_TIMESHEET", "TIMESHEET", t.getId()); return t;
    }
    public PageResult<Timesheet> timesheets(Long workItemId, long page, long size) {
        var q = new QueryWrapper<Timesheet>().eq(workItemId != null, "work_item_id", workItemId).orderByDesc("id");
        if (workItemId != null) access.requireView(db.get(WorkItem.class, workItemId).getCaseId());
        else if ("AGENT".equals(CurrentUserContext.require().role())) q.eq("agent_id", access.agent().getId());
        else role("ADMIN", "AGENT");
        return db.page(Timesheet.class, q, page, size);
    }
    @Transactional
    public void deleteTimesheet(Long id) {
        Timesheet t = db.get(Timesheet.class, id);
        if ("AGENT".equals(CurrentUserContext.require().role()) && !Objects.equals(t.getAgentId(), access.agent().getId())) CaseAccessServiceImpl.denied();
        db.mapper(Timesheet.class).deleteById(id); events.audit("DELETE_TIMESHEET", "TIMESHEET", id);
    }
}
