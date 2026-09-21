package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BusinessEvents {
    private final V2Store db;
    private final HttpServletRequest request;
    public BusinessEvents(V2Store db, HttpServletRequest request) { this.db = db; this.request = request; }
    public void notifyUser(Long userId, String type, String title, String business, Long id) {
        if (userId == null) return;
        Notification n = new Notification();
        n.setUserId(userId); n.setNotificationType(type); n.setTitle(title); n.setContent(title);
        n.setBusinessType(business); n.setBusinessId(id); n.setIsRead(0); n.setSendChannel("SYSTEM"); n.setSendStatus("SUCCESS");
        db.insert(n);
    }
    public void caseEvent(Long caseId, String type, String title) {
        CaseInfo c = db.get(CaseInfo.class, caseId);
        Set<Long> recipients = new LinkedHashSet<>();
        recipients.add(db.get(ClientProfile.class, c.getClientId()).getUserId());
        var agents = db.mapper(AgentProfile.class).selectList(new QueryWrapper<AgentProfile>()
            .and(q -> q.eq("id", c.getPrincipalAgentId()).or().inSql("id",
                "SELECT agent_id FROM case_assignment WHERE case_id = " + caseId + " AND is_current = 1 AND is_deleted = 0")));
        agents.forEach(a -> recipients.add(a.getUserId()));
        recipients.forEach(id -> notifyUser(id, type, title, "CASE", caseId));
    }
    public void audit(String operation, String business, Long id) {
        var user = CurrentUserContext.require();
        auditAs(user.userId(), user.role(), operation, business, id);
    }
    public void auditAs(Long userId, String role, String operation, String business, Long id) {
        OperationLog log = new OperationLog();
        if (userId != null) log.setUsername(db.get(SysUser.class, userId).getUsername());
        log.setUserId(userId); log.setRole(role); log.setModule(business); log.setOperation(operation);
        log.setBusinessType(business); log.setBusinessId(id); log.setResult("SUCCESS");
        log.setRequestMethod(request.getMethod()); log.setRequestPath(request.getRequestURI()); log.setIpAddress(request.getRemoteAddr());
        String ua = request.getHeader("User-Agent");
        log.setDeviceType(ua != null && ua.matches(".*(Mobile|Android|iPhone).*") ? "MOBILE" : "PC");
        db.insert(log);
    }
}
