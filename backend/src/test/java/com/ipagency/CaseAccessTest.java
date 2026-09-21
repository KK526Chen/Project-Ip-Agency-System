package com.ipagency;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ipagency.common.AuthenticatedUser;
import com.ipagency.entity.*;
import com.ipagency.service.V2Store;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class CaseAccessTest {
    @SuppressWarnings("unchecked")
    @Test void clientOwnershipUsesProfileIdNotUserId() {
        V2Store db = mock(V2Store.class); BaseMapper<CaseInfo> mapper = mock(BaseMapper.class);
        when(db.mapper(CaseInfo.class)).thenReturn(mapper);
        CaseInfo c = new CaseInfo(); c.setId(100L); c.setClientId(9L); when(mapper.selectById(100L)).thenReturn(c);
        ClientProfile p = new ClientProfile(); p.setId(9L); p.setUserId(50L);
        when(db.one(eq(ClientProfile.class), any())).thenReturn(p);
        CaseAccessServiceImpl access = new CaseAccessServiceImpl(db);
        assertThat(access.canViewCase(new AuthenticatedUser(50L, "CLIENT"), 100L)).isTrue();
        p.setId(50L);
        assertThat(access.canViewCase(new AuthenticatedUser(50L, "CLIENT"), 100L)).isFalse();
    }
    @SuppressWarnings("unchecked")
    @Test void currentCollaboratorsCanAccessButFormerAgentsCannot() {
        V2Store db = mock(V2Store.class); BaseMapper<CaseInfo> mapper = mock(BaseMapper.class);
        when(db.mapper(CaseInfo.class)).thenReturn(mapper);
        CaseInfo c = new CaseInfo(); c.setId(100L); c.setPrincipalAgentId(8L);
        when(mapper.selectById(100L)).thenReturn(c); when(db.get(CaseInfo.class,100L)).thenReturn(c);
        AgentProfile a = new AgentProfile(); a.setId(9L); a.setUserId(50L); when(db.one(eq(AgentProfile.class),any())).thenReturn(a);
        CaseAccessServiceImpl access = new CaseAccessServiceImpl(db);
        when(db.count(eq(CaseAssignment.class),any())).thenReturn(1L);
        assertThat(access.canViewCase(new AuthenticatedUser(50L,"AGENT"),100L)).isTrue();
        when(db.count(eq(CaseAssignment.class),any())).thenReturn(0L);
        assertThat(access.canViewCase(new AuthenticatedUser(50L,"AGENT"),100L)).isFalse();
        assertThat(access.canViewCase(new AuthenticatedUser(1L,"ADMIN"),100L)).isTrue();
        assertThat(access.canViewCase(new AuthenticatedUser(1L,"ADMIN"),999L)).isFalse();
    }
}
