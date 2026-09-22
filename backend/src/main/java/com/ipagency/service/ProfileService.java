package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class ProfileService {
    private final V2Store db;
    private final CaseAccessServiceImpl access;
    private final Input input;
    private final BusinessEvents events;
    public ProfileService(V2Store db, CaseAccessServiceImpl access, Input input, BusinessEvents events) {
        this.db = db; this.access = access; this.input = input; this.events = events;
    }
    public ClientProfile client() { return access.client(); }
    public AgentProfile agent() { return access.agent(); }
    @Transactional
    public ClientProfile saveClient(Map<String, Object> body) {
        role("CLIENT");
        Long userId = CurrentUserContext.require().userId();
        db.lock(SysUser.class, userId);
        ClientProfile p = db.one(ClientProfile.class, new QueryWrapper<ClientProfile>().eq("user_id", userId));
        if (p == null) { p = new ClientProfile(); p.setUserId(userId); }
        input.apply(body, p, "clientType clientName creditOrIdNo registeredAddress contactAddress primaryContactName primaryContactPhone primaryContactEmail industry technicalPreference invoiceTitle taxpayerNo bankName bankAccount");
        if (p.getId() == null) db.insert(p); else db.update(p);
        events.audit("UPDATE_PROFILE", "CLIENT", p.getId()); return p;
    }
    @Transactional
    public AgentProfile saveAgent(Map<String, Object> body) {
        role("AGENT");
        if (body.containsKey("agentId") || body.containsKey("id") || body.containsKey("userId") || body.containsKey("employeeNo"))
            throw new BusinessException("不允许修改字段: id/userId/employeeNo/agentId");
        AgentProfile p = access.agent();
        String employeeNo = p.getEmployeeNo(); Long userId = p.getUserId(); Long id = p.getId();
        input.apply(body, p, "licenseNo department professionalField practiceYears education ipcScope profile");
        p.setId(id); p.setUserId(userId); p.setEmployeeNo(employeeNo);
        db.update(p);
        events.audit("UPDATE_PROFILE", "AGENT", p.getId());
        return db.get(AgentProfile.class, p.getId());
    }
    public PageResult<ClientContact> contacts(long page, long size) {
        return db.page(ClientContact.class, new QueryWrapper<ClientContact>().eq("client_id", access.client().getId()).orderByDesc("id"), page, size);
    }
    @Transactional
    public ClientContact contact(Long id, Map<String, Object> body) {
        Long clientId = access.client().getId();
        ClientContact c = id == null ? new ClientContact() : db.get(ClientContact.class, id);
        if (id != null && !Objects.equals(c.getClientId(), clientId)) CaseAccessServiceImpl.denied();
        c.setClientId(clientId);
        input.apply(body, c, "name position phone email permissionScope remark");
        if (id == null) db.insert(c); else db.update(c);
        events.audit("SAVE_CONTACT", "CLIENT_CONTACT", c.getId()); return c;
    }
    @Transactional
    public void deleteContact(Long id) {
        ClientContact c = db.get(ClientContact.class, id);
        if (!Objects.equals(c.getClientId(), access.client().getId())) CaseAccessServiceImpl.denied();
        db.mapper(ClientContact.class).deleteById(id); events.audit("DELETE_CONTACT", "CLIENT_CONTACT", id);
    }
}
