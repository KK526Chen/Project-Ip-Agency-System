package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class CaseWorkflowService {
    private final V2Store db;
    private final CaseAccessServiceImpl access;
    private final Input input;
    private final ObjectMapper json;
    private final BusinessEvents events;
    public CaseWorkflowService(V2Store db, CaseAccessServiceImpl access, Input input, ObjectMapper json, BusinessEvents events) {
        this.db = db; this.access = access; this.input = input; this.json = json; this.events = events;
    }
    public PageResult<CaseInfo> list(long page, long size, String status, String caseType, String keyword) {
        var q = access.scope(new QueryWrapper<CaseInfo>(), "id", true);
        q.eq(status != null, "status", status).eq(caseType != null, "case_type", caseType)
            .like(keyword != null && !keyword.isBlank(), "case_name", keyword).orderByDesc("create_time", "id");
        return db.page(CaseInfo.class, q, page, size);
    }
    public CaseInfo detail(Long id) { access.requireView(id); return db.get(CaseInfo.class, id); }
    public <T> PageResult<T> children(Class<T> type, Long id, long page, long size, String order) {
        access.requireView(id);
        return db.page(type, new QueryWrapper<T>().eq("case_id", id).orderByAsc(order, "id"), page, size);
    }
    public static void state(String current, String... allowed) {
        if (!Arrays.asList(allowed).contains(current)) throw new BusinessException("当前状态不允许此操作: " + current, org.springframework.http.HttpStatus.CONFLICT);
    }
    @Transactional
    public CaseInfo save(Long id, Map<String, Object> body) {
        role("CLIENT");
        CaseInfo c;
        if (id == null) {
            c = new CaseInfo(); c.setClientId(access.client().getId()); c.setStatus("SUBMITTED");
            c.setPriorityLevel("MEDIUM"); c.setConfidentialReview(0);
        } else { access.requireView(id); c = db.lock(CaseInfo.class, id); state(c.getStatus(), "SUBMITTED", "RETURNED"); }
        Map<String, Object> fields = new LinkedHashMap<>(body);
        Object parties = fields.remove("parties"), priorities = fields.remove("priorities");
        input.apply(fields, c, "caseName serviceProductId caseType technicalField priorityLevel confidentialReview description");
        if (c.getServiceProductId() != null && !Integer.valueOf(1).equals(db.get(ServiceProduct.class, c.getServiceProductId()).getStatus()))
            throw new BusinessException("服务已下架");
        if (id == null) db.insert(c); else db.update(c);
        if (parties != null) saveParties(c.getId(), parties);
        if (priorities != null) savePriorities(c.getId(), priorities);
        events.audit(id == null ? "CREATE_CASE" : "UPDATE_CASE", "CASE", c.getId());
        return c;
    }
    private List<Map<String, Object>> rows(Object value) {
        if (!(value instanceof List<?> list) || list.size() > 100) throw new BusinessException("当事人或优先权必须为不超过 100 条的数组");
        try { return json.convertValue(list, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}); }
        catch (Exception e) { throw new BusinessException("明细格式不正确"); }
    }
    private void saveParties(Long id, Object value) {
        db.mapper(CaseParty.class).delete(new QueryWrapper<CaseParty>().eq("case_id", id));
        for (var row : rows(value)) {
            CaseParty p = new CaseParty(); p.setCaseId(id); p.setIsPrimary(0);
            input.apply(row, p, "partyType name idNo nationality address isPrimary remark"); db.insert(p);
        }
    }
    private void savePriorities(Long id, Object value) {
        // The unique key includes logically deleted records: revive existing priorities instead of duplicating them.
        Set<String> numbers = new HashSet<>();
        var mapper = (com.ipagency.mapper.CasePriorityMapper) db.mapper(CasePriority.class);
        for (var row : rows(value)) {
            CasePriority p = new CasePriority(); p.setCaseId(id);
            input.apply(row, p, "country priorityNo priorityDate"); db.validate(p);
            if (!numbers.add(p.getPriorityNo())) throw new BusinessException("优先权号重复");
            var old = mapper.findIncludingDeleted(id, p.getPriorityNo());
            if (old == null) db.insert(p); else { p.setId(old.getId()); mapper.restore(p); }
        }
        mapper.delete(new QueryWrapper<CasePriority>().eq("case_id", id).notIn(!numbers.isEmpty(), "priority_no", numbers));
    }
    @Transactional
    public CaseInfo submit(Long id) {
        role("CLIENT"); access.requireView(id);
        CaseInfo c = db.lock(CaseInfo.class, id); state(c.getStatus(), "SUBMITTED", "RETURNED");
        if (db.count(CaseParty.class, new QueryWrapper<CaseParty>().eq("case_id", id).eq("party_type", "APPLICANT")) == 0)
            throw new BusinessException("提交前请填写至少一名申请人");
        c.setStatus("PENDING_REVIEW"); c.setSubmitTime(LocalDateTime.now()); c.setCurrentStage("委托提交"); db.update(c);
        stageEvent(id, "SUBMISSION", "委托提交", "COMPLETED");
        events.caseEvent(id, "CASE_STATUS", "案件已提交审核"); events.audit("SUBMIT_CASE", "CASE", id); return c;
    }
    public CaseStage stageEvent(Long id, String type, String name, String status) {
        CaseStage s = new CaseStage(); s.setCaseId(id); s.setStageType(type); s.setStageName(name);
        s.setStartTime(LocalDateTime.now()); s.setStatus(status); s.setHandlerUserId(CurrentUserContext.require().userId());
        if ("COMPLETED".equals(status)) s.setEndTime(s.getStartTime());
        return db.insert(s);
    }
    @Transactional
    public ReviewRecord review(Long caseId, Map<String, Object> body) {
        role("ADMIN");
        CaseInfo c = db.lock(CaseInfo.class, caseId);
        state(c.getStatus(), "PENDING_REVIEW");
        String result = Input.text(body, "reviewResult"); state(result, "APPROVED", "RETURNED", "REJECTED");
        ReviewRecord r = record(caseId, "CASE", caseId, "CASE_ACCEPTANCE", result, (String)body.get("reviewComment"));
        if ("APPROVED".equals(result)) {
            c.setStatus("PENDING_ASSIGNMENT"); c.setAcceptTime(LocalDateTime.now());
            if (c.getCaseNo() == null) c.setCaseNo("IP-" + LocalDateTime.now().getYear() + "-" + c.getId());
            c.setCurrentStage("受理"); stageEvent(caseId, "ACCEPTANCE", "受理", "COMPLETED");
        } else c.setStatus("RETURNED");
        db.update(c); events.caseEvent(caseId, "CASE_STATUS", "案件审核结果: " + result);
        events.audit("APPROVED".equals(result) ? "APPROVE_CASE" : "RETURN_CASE", "CASE", caseId); return r;
    }
    public ReviewRecord record(Long caseId, String target, Long targetId, String type, String result, String comment) {
        ReviewRecord r = new ReviewRecord(); r.setCaseId(caseId); r.setTargetType(target); r.setTargetId(targetId);
        r.setReviewerUserId(CurrentUserContext.require().userId()); r.setReviewType(type); r.setReviewResult(result);
        r.setReviewComment(comment); r.setReviewTime(LocalDateTime.now()); return db.insert(r);
    }
    @Transactional
    public CaseAssignment assign(Long id, Long agentId, String reason, boolean reassign) {
        role("ADMIN"); CaseInfo c = db.lock(CaseInfo.class, id);
        if (!reassign) state(c.getStatus(), "PENDING_ASSIGNMENT");
        else if (c.getPrincipalAgentId() == null || Set.of("CLOSED", "WITHDRAWN", "EXPIRED").contains(c.getStatus()))
            throw new BusinessException("当前案件不能重新分配");
        AgentProfile agent = db.get(AgentProfile.class, agentId);
        SysUser u = db.get(SysUser.class, agent.getUserId());
        if (!"AGENT".equals(u.getRole()) || !Integer.valueOf(1).equals(u.getStatus())) throw new BusinessException("代理人不可用");
        if (Objects.equals(c.getPrincipalAgentId(), agentId)) throw new BusinessException("代理人未发生变化");
        Long previousAgentId = c.getPrincipalAgentId();
        db.mapper(CaseAssignment.class).update(null, new UpdateWrapper<CaseAssignment>().eq("case_id", id)
            .eq("assignment_role", "PRINCIPAL").eq("is_current", 1).set("is_current", 0).set("end_time", LocalDateTime.now()));
        CaseAssignment a = new CaseAssignment(); a.setCaseId(id); a.setAgentId(agentId); a.setAssignedByUserId(CurrentUserContext.require().userId());
        a.setAssignmentRole("PRINCIPAL"); a.setAssignTime(LocalDateTime.now()); a.setIsCurrent(1); a.setReason(reason); db.insert(a);
        c.setPrincipalAgentId(agentId); if (!reassign) c.setStatus("PROCESSING"); db.update(c);
        // Move open principal tasks with the case; completed task history is retained.
        db.mapper(DeadlineTask.class).update(null, new UpdateWrapper<DeadlineTask>().eq("case_id", id).ne("status", "COMPLETED")
            .and(q -> q.isNull("agent_id").or(previousAgentId != null).eq(previousAgentId != null, "agent_id", previousAgentId)).set("agent_id", agentId));
        events.caseEvent(id, "CASE_STATUS", "案件已分配代理人"); events.audit(reassign ? "REASSIGN_AGENT" : "ASSIGN_AGENT", "CASE", id); return a;
    }
    @Transactional
    public CaseStage stage(Long caseId, Long stageId, Map<String, Object> body) {
        access.requireManage(caseId); CaseInfo c = db.lock(CaseInfo.class, caseId);
        CaseStage s = stageId == null ? new CaseStage() : db.get(CaseStage.class, stageId);
        if (stageId != null && !Objects.equals(s.getCaseId(), caseId)) CaseAccessServiceImpl.denied();
        if (stageId != null) state(s.getStatus(), "NOT_STARTED", "IN_PROGRESS");
        s.setCaseId(caseId); s.setHandlerUserId(CurrentUserContext.require().userId());
        if (stageId == null) { s.setStartTime(LocalDateTime.now()); s.setStatus("IN_PROGRESS"); }
        input.apply(body, s, "stageType stageName startTime endTime status description");
        if ("COMPLETED".equals(s.getStatus()) && s.getEndTime() == null) s.setEndTime(LocalDateTime.now());
        if (s.getEndTime() != null && s.getEndTime().isBefore(s.getStartTime())) throw new BusinessException("阶段结束时间早于开始时间");
        if (stageId == null) db.insert(s); else db.update(s);
        c.setCurrentStage(s.getStageName());
        String next = Map.of("PRELIMINARY_EXAM", "FORMAL_EXAM", "SUBSTANTIVE_EXAM", "SUBSTANTIVE_EXAM", "GRANT", "GRANTED", "CERTIFICATE", "CLOSED").get(s.getStageType());
        state(c.getStatus(), "PROCESSING", "FORMAL_EXAM", "SUBSTANTIVE_EXAM", "PRELIMINARY_PASSED", "GRANTED", "REEXAMINATION");
        if (next != null) c.setStatus(next);
        db.update(c); events.caseEvent(caseId, "CASE_STATUS", "案件阶段更新: " + s.getStageName()); events.audit("UPDATE_STAGE", "CASE", caseId); return s;
    }
}
