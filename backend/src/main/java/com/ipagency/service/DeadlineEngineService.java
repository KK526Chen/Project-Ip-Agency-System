package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class DeadlineEngineService {
    private static final List<String[]> MILESTONES = List.of(
        new String[]{"CUSTOMER_MATERIAL", "客户意见", "22"},
        new String[]{"DRAFT", "初稿", "15"},
        new String[]{"REVIEW", "内部审核", "10"},
        new String[]{"CLIENT_CONFIRM", "客户确认", "5"},
        new String[]{"SUBMISSION", "最终提交", "3"}
    );
    private final V2Store db; private final CaseAccessServiceImpl access; private final DeadlineService deadlines;
    private final WorkItemService workItems; private final BusinessEvents events;
    public DeadlineEngineService(V2Store db, CaseAccessServiceImpl access, DeadlineService deadlines, WorkItemService workItems, BusinessEvents events) {
        this.db = db; this.access = access; this.deadlines = deadlines; this.workItems = workItems; this.events = events;
    }
    public void recordHistory(Long deadlineId, String type, LocalDateTime oldValue, LocalDateTime newValue, String reason) {
        DeadlineHistory h = new DeadlineHistory(); h.setDeadlineId(deadlineId); h.setChangeType(type);
        h.setOldValue(oldValue); h.setNewValue(newValue); h.setOperatorUserId(CurrentUserContext.require().userId()); h.setReason(reason);
        db.insert(h);
    }
    public void spawnOfficialMilestones(DeadlineTask task) {
        if (task.getOfficialDeadline() == null) return;
        if (db.count(WorkItem.class, new QueryWrapper<WorkItem>().eq("deadline_id", task.getId())) > 0) return;
        for (String[] row : MILESTONES) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("caseId", task.getCaseId()); body.put("deadlineId", task.getId()); body.put("workType", row[0]);
            body.put("title", row[1] + ": " + task.getTaskName());
            body.put("dueTime", task.getOfficialDeadline().minusDays(Long.parseLong(row[2])).toString());
            body.put("assigneeAgentId", task.getAgentId());
            try { workItems.save(null, body); } catch (Exception ignored) { }
        }
    }
    public List<DeadlineHistory> history(Long id) {
        DeadlineTask d = db.get(DeadlineTask.class, id); access.requireView(d.getCaseId());
        return db.mapper(DeadlineHistory.class).selectList(new QueryWrapper<DeadlineHistory>().eq("deadline_id", id).orderByAsc("id"));
    }
    @Transactional
    public DeadlineTask adjust(Long id, Map<String, Object> body) {
        DeadlineTask d = db.lock(DeadlineTask.class, id); access.requireManage(d.getCaseId());
        if ("COMPLETED".equals(d.getStatus())) throw new BusinessException("DEADLINE_ALREADY_COMPLETED: 已完成时限不能调整", HttpStatus.CONFLICT);
        LocalDateTime old = d.getOfficialDeadline();
        LocalDateTime neu = LocalDateTime.parse(Input.text(body, "officialDeadline"));
        String reason = Input.text(body, "reason");
        DeadlineAdjustment adj = new DeadlineAdjustment(); adj.setDeadlineId(id); adj.setOldValue(old); adj.setNewValue(neu);
        adj.setOperatorUserId(CurrentUserContext.require().userId()); adj.setReason(reason); adj.setAttachment((String) body.get("attachment"));
        db.insert(adj);
        recordHistory(id, "ADJUST", old, neu, reason);
        Map<String, Object> fields = new LinkedHashMap<>(); fields.put("officialDeadline", neu.toString());
        DeadlineTask saved = deadlines.save(id, fields);
        events.audit("ADJUST_DEADLINE", "DEADLINE", id); return saved;
    }
    public PageResult<DeadlineRule> rules(long page, long size) { role("ADMIN"); return db.page(DeadlineRule.class, new QueryWrapper<DeadlineRule>().orderByAsc("id"), page, size); }
    @Transactional
    public DeadlineRule saveRule(Long id, Map<String, Object> body) {
        role("ADMIN"); DeadlineRule r = id == null ? new DeadlineRule() : db.get(DeadlineRule.class, id);
        r.setRuleCode(body.getOrDefault("ruleCode", r.getRuleCode() == null ? "RULE" : r.getRuleCode()).toString());
        r.setBusinessType((String) body.get("businessType")); r.setTriggerEvent((String) body.get("triggerEvent"));
        if (body.get("baseDays") != null) r.setBaseDays(Integer.valueOf(body.get("baseDays").toString()));
        r.setDayType((String) body.getOrDefault("dayType", "CALENDAR"));
        if (body.get("internalOffsetDays") != null) r.setInternalOffsetDays(Integer.valueOf(body.get("internalOffsetDays").toString()));
        r.setAdjustmentPolicy((String) body.get("adjustmentPolicy"));
        if (body.get("effectiveFrom") != null && !body.get("effectiveFrom").toString().isBlank()) r.setEffectiveFrom(LocalDate.parse(body.get("effectiveFrom").toString()));
        if (body.get("effectiveTo") != null && !body.get("effectiveTo").toString().isBlank()) r.setEffectiveTo(LocalDate.parse(body.get("effectiveTo").toString()));
        if (id == null) { r.setVersionNo(1); db.insert(r); } else db.update(r);
        return r;
    }
    public PageResult<BusinessCalendar> calendars(long page, long size) { role("ADMIN"); return db.page(BusinessCalendar.class, new QueryWrapper<BusinessCalendar>().orderByAsc("id"), page, size); }
    public List<CalendarDay> days(Long calendarId, String from, String to) {
        role("ADMIN"); db.get(BusinessCalendar.class, calendarId);
        var q = new QueryWrapper<CalendarDay>().eq("calendar_id", calendarId);
        if (from != null && !from.isBlank()) q.ge("day_date", from);
        if (to != null && !to.isBlank()) q.le("day_date", to);
        return db.mapper(CalendarDay.class).selectList(q.orderByAsc("day_date"));
    }
    @Transactional
    public CalendarDay upsertDay(Long calendarId, Map<String, Object> body) {
        role("ADMIN"); db.get(BusinessCalendar.class, calendarId);
        LocalDate date = LocalDate.parse(Input.text(body, "dayDate"));
        CalendarDay day = db.one(CalendarDay.class, new QueryWrapper<CalendarDay>().eq("calendar_id", calendarId).eq("day_date", date));
        if (day == null) { day = new CalendarDay(); day.setCalendarId(calendarId); day.setDayDate(date); }
        day.setIsWorkday(body.get("isWorkday") == null ? 1 : Integer.valueOf(body.get("isWorkday").toString()));
        day.setHolidayName((String) body.get("holidayName"));
        if (body.get("overrideType") != null && !body.get("overrideType").toString().isBlank()) {
            day.setOverrideType(body.get("overrideType").toString());
        } else {
            day.setOverrideType(Integer.valueOf(0).equals(day.getIsWorkday()) ? "HOLIDAY" : "MAKEUP");
        }
        if (day.getId() == null) db.insert(day); else db.update(day); return day;
    }
    public LocalDate addBusinessDays(LocalDate start, int days) {
        BusinessCalendar cal = db.one(BusinessCalendar.class, new QueryWrapper<BusinessCalendar>().eq("status", 1).last("LIMIT 1"));
        LocalDate cursor = start; int added = 0;
        while (added < days) {
            cursor = cursor.plusDays(1);
            CalendarDay override = cal == null ? null : db.one(CalendarDay.class, new QueryWrapper<CalendarDay>().eq("calendar_id", cal.getId()).eq("day_date", cursor));
            boolean work = override == null ? cursor.getDayOfWeek().getValue() < 6 : Integer.valueOf(1).equals(override.getIsWorkday());
            if (work) added++;
        }
        return cursor;
    }
}
