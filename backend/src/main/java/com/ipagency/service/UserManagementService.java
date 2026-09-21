package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class UserManagementService {
    private final V2Store db; private final Input input; private final BusinessEvents events;
    public UserManagementService(V2Store db, Input input, BusinessEvents events) { this.db = db; this.input = input; this.events = events; }
    public PageResult<SysUser> list(long page, long size, String keyword, String userRole) {
        role("ADMIN"); return db.page(SysUser.class, new QueryWrapper<SysUser>().eq(userRole != null, "role", userRole)
            .like(keyword != null, "username", keyword).orderByDesc("id"), page, size);
    }
    @Transactional public SysUser save(Long id, Map<String, Object> body) {
        role("ADMIN"); SysUser u = id == null ? new SysUser() : db.lock(SysUser.class, id);
        var fields = new java.util.LinkedHashMap<>(body); Object password = fields.remove("password");
        input.apply(fields, u, id == null ? "username realName role phone email status" : "realName phone email status");
        if (id == null) {
            if (!(password instanceof String s) || s.length() < 6 || s.length() > 72) throw new BusinessException("密码长度必须为 6 到 72 位");
            u.setPasswordHash(new BCryptPasswordEncoder().encode((String) password)); if (u.getStatus() == null) u.setStatus(1); db.insert(u);
            if ("CLIENT".equals(u.getRole())) {
                ClientProfile p = new ClientProfile(); p.setUserId(u.getId()); p.setClientName(u.getRealName()); p.setClientType("INDIVIDUAL"); db.insert(p);
            } else if ("AGENT".equals(u.getRole())) {
                AgentProfile p = new AgentProfile(); p.setUserId(u.getId()); p.setEmployeeNo("AGENT-" + u.getId()); p.setPracticeYears(0); db.insert(p);
            }
        } else {
            if (password != null) throw new BusinessException("此接口不修改密码");
            if (u.getId().equals(CurrentUserContext.require().userId()) && !Integer.valueOf(1).equals(u.getStatus())) throw new BusinessException("不能停用当前账户");
            db.update(u);
        }
        events.audit(id == null ? "CREATE_USER" : "UPDATE_USER", "USER", u.getId()); return u;
    }
}
