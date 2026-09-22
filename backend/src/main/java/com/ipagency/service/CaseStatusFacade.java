package com.ipagency.service;

import com.ipagency.entity.CaseInfo;
import org.springframework.stereotype.Service;

/** C 对 case_info 状态/主办/阶段的唯一写入入口，避免散落 Mapper 更新。 */
@Service
public class CaseStatusFacade {
    private final V2Store db;
    public CaseStatusFacade(V2Store db) { this.db = db; }
    public CaseInfo lock(Long caseId) { return db.lock(CaseInfo.class, caseId); }
    public CaseInfo setPrincipalAgent(CaseInfo c, Long agentId) {
        c.setPrincipalAgentId(agentId); return db.update(c);
    }
    public CaseInfo transition(CaseInfo c, String status) {
        c.setStatus(status); return db.update(c);
    }
    public CaseInfo setCurrentStage(CaseInfo c, String stageName) {
        c.setCurrentStage(stageName); return db.update(c);
    }
    public CaseInfo save(CaseInfo c) { return db.update(c); }
}
