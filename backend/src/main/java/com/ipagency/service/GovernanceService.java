package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class GovernanceService {
    private final V2Store db; private final CaseAccessServiceImpl access; private final BusinessEvents events; private final DomainEventPublisher publisher;
    public GovernanceService(V2Store db, CaseAccessServiceImpl access, BusinessEvents events, DomainEventPublisher publisher) {
        this.db = db; this.access = access; this.events = events; this.publisher = publisher;
    }
    public ExceptionCase openException(Long caseId, String type, String detail, Long relatedId) {
        ExceptionCase e = new ExceptionCase(); e.setCaseId(caseId); e.setExceptionType(type); e.setTitle(type);
        e.setDetail(detail); e.setStatus("OPEN"); e.setRelatedId(relatedId); db.insert(e);
        publisher.publish("EXCEPTION_CREATED", caseId, e.getId(), type); return e;
    }
    public PageResult<ExceptionCase> exceptions(String status, long page, long size) {
        role("ADMIN");
        return db.page(ExceptionCase.class, new QueryWrapper<ExceptionCase>().eq(status != null, "status", status).orderByDesc("id"), page, size);
    }
    @Transactional
    public ExceptionCase assign(Long id, Long userId) {
        role("ADMIN"); ExceptionCase e = db.lock(ExceptionCase.class, id); e.setAssigneeUserId(userId); e.setStatus("ASSIGNED"); db.update(e);
        events.audit("ASSIGN_EXCEPTION", "EXCEPTION", id); return e;
    }
    @Transactional
    public ExceptionCase resolve(Long id, String note) {
        role("ADMIN"); ExceptionCase e = db.lock(ExceptionCase.class, id);
        if (note == null || note.isBlank()) throw new BusinessException("解决异常必须填写 resolution_note");
        e.setResolutionNote(note); e.setResolvedBy(CurrentUserContext.require().userId()); e.setResolvedAt(LocalDateTime.now()); e.setStatus("RESOLVED");
        db.update(e); events.audit("RESOLVE_EXCEPTION", "EXCEPTION", id); return e;
    }
    public PageResult<RiskRecord> risks(Long caseId, long page, long size) {
        if (caseId != null) access.requireView(caseId);
        var q = access.scope(new QueryWrapper<RiskRecord>(), "case_id", false).eq(caseId != null, "case_id", caseId).orderByDesc("id");
        return db.page(RiskRecord.class, q, page, size);
    }
    public RiskRecord raise(Long caseId, String type, String level, String title) {
        RiskRecord r = new RiskRecord(); r.setCaseId(caseId); r.setRiskType(type); r.setLevelCode(level); r.setTitle(title); r.setStatus("OPEN");
        db.insert(r); publisher.publish("RISK_CREATED", caseId, r.getId(), title); return r;
    }
    public List<DataQualityIssue> scan() {
        role("ADMIN");
        db.mapper(DataQualityIssue.class).delete(new QueryWrapper<DataQualityIssue>().eq("status", "OPEN"));
        List<DataQualityIssue> found = new ArrayList<>();
        for (CaseInfo c : db.mapper(CaseInfo.class).selectList(new QueryWrapper<CaseInfo>().eq("status", "PROCESSING").isNull("principal_agent_id")))
            found.add(issue("NO_PRINCIPAL", c.getId(), "PROCESSING 案件无主办代理人"));
        found.addAll(scanDuplicatePrincipals());
        for (CaseDocument d : db.mapper(CaseDocument.class).selectList(new QueryWrapper<CaseDocument>().eq("document_type", "OFFICIAL").eq("ocr_status", "MANUAL").isNotNull("official_deadline"))) {
            if (db.count(DeadlineTask.class, new QueryWrapper<DeadlineTask>().eq("document_id", d.getId()).eq("deadline_type", "OFFICIAL_RESPONSE")) == 0)
                found.add(issue("OFFICIAL_WITHOUT_DEADLINE", d.getCaseId(), "官文已确认期限但无 deadline_task"));
        }
        for (DeadlineTask t : db.mapper(DeadlineTask.class).selectList(new QueryWrapper<DeadlineTask>().eq("status", "COMPLETED").isNull("completed_time")))
            found.add(issue("COMPLETED_WITHOUT_TIME", t.getCaseId(), "COMPLETED deadline 无 completed_time"));
        found.forEach(db::insert);
        return found;
    }
    private List<DataQualityIssue> scanDuplicatePrincipals() {
        List<DataQualityIssue> list = new ArrayList<>();
        var rows = db.mapper(CaseAssignment.class).selectMaps(new QueryWrapper<CaseAssignment>().select("case_id", "COUNT(*) AS total")
            .eq("assignment_role", "PRINCIPAL").eq("is_current", 1).groupBy("case_id").having("COUNT(*) > 1"));
        for (var row : rows) list.add(issue("MULTI_PRINCIPAL", Long.valueOf(row.get("case_id").toString()), "当前 PRINCIPAL 超过 1 条"));
        return list;
    }
    private DataQualityIssue issue(String code, Long caseId, String detail) {
        DataQualityIssue i = new DataQualityIssue(); i.setIssueCode(code); i.setCaseId(caseId); i.setDetail(detail); i.setStatus("OPEN"); return i;
    }
    public List<Map<String, Object>> recommend(Long caseId) {
        role("ADMIN"); CaseInfo c = db.get(CaseInfo.class, caseId);
        List<AgentProfile> agents = db.mapper(AgentProfile.class).selectList(new QueryWrapper<AgentProfile>()
            .inSql("user_id", "SELECT id FROM sys_user WHERE role='AGENT' AND status=1 AND is_deleted=0"));
        Map<Long, Long> caseLoad = groupedCount(db.mapper(CaseAssignment.class).selectMaps(
            new QueryWrapper<CaseAssignment>().select("agent_id", "COUNT(*) AS total").eq("is_current", 1).groupBy("agent_id")), "agent_id");
        Map<Long, Long> upcomingLoad = groupedCount(db.mapper(DeadlineTask.class).selectMaps(
            new QueryWrapper<DeadlineTask>().select("agent_id", "COUNT(*) AS total").ne("status", "COMPLETED")
                .le("official_deadline", LocalDateTime.now().plusDays(7)).groupBy("agent_id")), "agent_id");
        Map<Long, Long> openWiLoad = Map.of();
        try {
            openWiLoad = groupedCount(db.mapper(WorkItem.class).selectMaps(
                new QueryWrapper<WorkItem>().select("assignee_agent_id", "COUNT(*) AS total").notIn("status", "DONE", "CANCELLED").groupBy("assignee_agent_id")), "assignee_agent_id");
        } catch (Exception ignored) { }
        List<Map<String, Object>> result = new ArrayList<>();
        for (AgentProfile a : agents) {
            long cases = caseLoad.getOrDefault(a.getId(), 0L);
            long openWi = openWiLoad.getOrDefault(a.getId(), 0L);
            long upcoming = upcomingLoad.getOrDefault(a.getId(), 0L);
            int score = 100;
            List<String> reasons = new ArrayList<>();
            if (textOverlap(a.getProfessionalField(), c.getTechnicalField())) { score += 20; reasons.add("专业领域匹配"); }
            if (textOverlap(a.getIpcScope(), c.getTechnicalField())) { score += 15; reasons.add("IPC 范围匹配"); }
            String expectedDept = departmentFor(c.getCaseType());
            if (expectedDept != null && expectedDept.equalsIgnoreCase(nullToEmpty(a.getDepartment()))) {
                score += 10; reasons.add("案件类型与部门对应");
            }
            if (a.getPracticeYears() != null && a.getPracticeYears() >= 5) { score += 5; reasons.add("执业经验充足"); }
            score -= (int) Math.min(40, cases * 5 + openWi * 3 + upcoming * 4);
            if (cases == 0) reasons.add("当前无在办案件");
            else if (cases <= 2) reasons.add("在办负荷较低");
            if (upcoming == 0) reasons.add("近7日时限较少");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("agentId", a.getId()); row.put("employeeNo", a.getEmployeeNo()); row.put("department", a.getDepartment());
            row.put("professionalField", a.getProfessionalField()); row.put("currentCaseCount", cases);
            row.put("openWorkItemCount", openWi); row.put("deadline7DaysCount", upcoming); row.put("matchScore", score); row.put("reasons", reasons);
            result.add(row);
        }
        result.sort((x, y) -> Integer.compare((Integer) y.get("matchScore"), (Integer) x.get("matchScore")));
        return result;
    }
    private static String departmentFor(String caseType) {
        if (caseType == null || caseType.isBlank()) return null;
        if (caseType.contains("TRADEMARK")) return "TRADEMARK";
        if (caseType.contains("COPYRIGHT")) return "COPYRIGHT";
        if (caseType.contains("LITIGATION") || caseType.contains("INVALID") || caseType.contains("OPPOSITION")) return "LITIGATION";
        if (caseType.contains("PATENT") || caseType.contains("UTILITY") || caseType.contains("DESIGN")) return "PATENT";
        return null;
    }
    private static boolean textOverlap(String left, String right) {
        if (left == null || right == null || left.isBlank() || right.isBlank()) return false;
        String a = left.toLowerCase(Locale.ROOT);
        String b = right.toLowerCase(Locale.ROOT);
        if (a.contains(b) || b.contains(a)) return true;
        for (String token : b.split("[\\s,，、/;]+")) {
            if (token.length() >= 2 && a.contains(token)) return true;
        }
        return false;
    }
    private static Map<Long, Long> groupedCount(List<Map<String, Object>> rows, String idKey) {
        Map<Long, Long> map = new HashMap<>();
        for (var row : rows) {
            Object id = row.get(idKey); Object total = row.get("total");
            if (id == null || total == null) continue;
            map.put(Long.valueOf(id.toString()), ((Number) total).longValue());
        }
        return map;
    }
    private static String nullToEmpty(String value) { return value == null ? "" : value; }
}
