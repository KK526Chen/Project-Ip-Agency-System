package com.ipagency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CaseAccessServiceImpl implements CaseAccessService {
    private final V2Store db;
    public CaseAccessServiceImpl(V2Store db) { this.db = db; }
    public static void role(String... roles) {
        if (!java.util.Arrays.asList(roles).contains(CurrentUserContext.require().role())) denied();
    }
    public static void denied() { throw new BusinessException("无权访问该业务数据", HttpStatus.FORBIDDEN); }
    public ClientProfile client() {
        role("CLIENT");
        ClientProfile p = db.one(ClientProfile.class, new QueryWrapper<ClientProfile>().eq("user_id", CurrentUserContext.require().userId()));
        if (p == null) throw new BusinessException("请先完善客户资料");
        return p;
    }
    public AgentProfile agent() {
        role("AGENT");
        AgentProfile p = db.one(AgentProfile.class, new QueryWrapper<AgentProfile>().eq("user_id", CurrentUserContext.require().userId()));
        if (p == null) throw new BusinessException("代理人资料不存在");
        return p;
    }
    public boolean assigned(Long caseId, Long agentId) {
        CaseInfo c = db.get(CaseInfo.class, caseId);
        return Objects.equals(agentId, c.getPrincipalAgentId()) || db.count(CaseAssignment.class,
                new QueryWrapper<CaseAssignment>().eq("case_id", caseId).eq("agent_id", agentId).eq("is_current", 1)) > 0;
    }
    @Override public boolean canViewCase(AuthenticatedUser user, Long caseId) {
        CaseInfo c = db.mapper(CaseInfo.class).selectById(caseId);
        if (c == null) return false;
        if (user.isAdmin()) return true;
        if ("CLIENT".equals(user.role())) {
            ClientProfile p = db.one(ClientProfile.class, new QueryWrapper<ClientProfile>().eq("user_id", user.userId()));
            return p != null && Objects.equals(p.getId(), c.getClientId());
        }
        if ("AGENT".equals(user.role())) {
            AgentProfile p = db.one(AgentProfile.class, new QueryWrapper<AgentProfile>().eq("user_id", user.userId()));
            return p != null && assigned(caseId, p.getId());
        }
        return false;
    }
    @Override public boolean canManageCase(AuthenticatedUser user, Long id) {
        return (user.isAdmin() || "AGENT".equals(user.role())) && canViewCase(user, id);
    }
    @Override public void requireView(Long id) { if (!canViewCase(CurrentUserContext.require(), id)) denied(); }
    @Override public void requireManage(Long id) { if (!canManageCase(CurrentUserContext.require(), id)) denied(); }
    // SQL fragments contain only trusted numeric profile IDs resolved from the authenticated user.
    public <T> QueryWrapper<T> scope(QueryWrapper<T> q, String caseColumn, boolean cases) {
        if (CurrentUserContext.require().isAdmin()) return q;
        String predicate;
        if ("CLIENT".equals(CurrentUserContext.require().role())) predicate = "client_id = " + client().getId();
        else if ("AGENT".equals(CurrentUserContext.require().role())) {
            Long id = agent().getId();
            predicate = "(principal_agent_id = " + id + " OR id IN (SELECT case_id FROM case_assignment WHERE agent_id = " + id + " AND is_current = 1 AND is_deleted = 0))";
        } else { denied(); return q; }
        return cases ? q.apply(predicate) : q.inSql(caseColumn, "SELECT id FROM case_info WHERE is_deleted = 0 AND " + predicate);
    }
}
