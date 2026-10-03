package com.ipagency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import jakarta.validation.Validation;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ModuleAProfileTest {
    private final V2Store db = mock(V2Store.class);
    private final BusinessEvents events = mock(BusinessEvents.class);
    private final CaseAccessServiceImpl access = new CaseAccessServiceImpl(db);
    private final ProfileService profiles = new ProfileService(db, access,
        new Input(new ObjectMapper().findAndRegisterModules()), events);

    @AfterEach void clear() { CurrentUserContext.clear(); }
    private void asClient() { CurrentUserContext.set(new AuthenticatedUser(50L, "CLIENT")); }
    private ClientProfile existingClient() {
        asClient(); ClientProfile p = new ClientProfile(); p.setId(9L); p.setUserId(50L);
        when(db.one(eq(ClientProfile.class), any())).thenReturn(p); return p;
    }

    @Test void newClientCanOpenProfileWithoutCreatingDatabaseRow() {
        asClient();
        ClientProfile blank = profiles.client();
        assertThat(blank.getId()).isNull();
        assertThat(blank.getUserId()).isEqualTo(50L);
        assertThat(blank.getClientType()).isEqualTo("INDIVIDUAL");
        verify(db, never()).insert(any());
        assertThatThrownBy(access::client).isInstanceOf(BusinessException.class).hasMessage("请先完善客户资料");
    }

    @Test void firstSaveUsesAuthenticatedUserAndGeneratedProfileId() {
        asClient();
        doAnswer(call -> { ((ClientProfile) call.getArgument(0)).setId(9L); return call.getArgument(0); })
            .when(db).insert(any(ClientProfile.class));
        ClientProfile p = profiles.saveClient(Map.of("clientType", "COMPANY", "clientName", "示例企业"));
        assertThat(p.getUserId()).isEqualTo(50L); assertThat(p.getId()).isEqualTo(9L);
        verify(db).lock(SysUser.class, 50L);
        verify(events).audit("UPDATE_PROFILE", "CLIENT", 9L);
    }

    @Test void clientCannotAssignProfileOwnershipOrContactOwnership() {
        existingClient();
        for (String key : new String[]{"id", "userId", "isDeleted"})
            assertThatThrownBy(() -> profiles.saveClient(Map.of(key, 99L)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("不允许修改字段");
        assertThatThrownBy(() -> profiles.contact(null, Map.of("clientId", 99L, "name", "越权")))
            .isInstanceOf(BusinessException.class).hasMessageContaining("不允许修改字段");
        verify(db, never()).insert(any()); verify(db, never()).update(any());
    }

    @Test void otherClientsContactsCannotBeUpdatedOrDeleted() {
        existingClient(); ClientContact other = new ClientContact(); other.setId(7L); other.setClientId(99L);
        when(db.get(ClientContact.class, 7L)).thenReturn(other);
        assertThatThrownBy(() -> profiles.contact(7L, Map.of("name", "篡改")))
            .isInstanceOf(BusinessException.class).hasMessage("无权访问该业务数据");
        assertThatThrownBy(() -> profiles.deleteContact(7L))
            .isInstanceOf(BusinessException.class).hasMessage("无权访问该业务数据");
        verify(db, never()).update(any()); verify(db, never()).mapper(any()); verifyNoInteractions(events);
    }

    @Test void newContactBelongsToClientProfileRatherThanLoginUser() {
        existingClient();
        ClientContact c = profiles.contact(null, Map.of("name", "张三", "permissionScope", "FEE_ONLY"));
        assertThat(c.getClientId()).isEqualTo(9L); assertThat(c.getName()).isEqualTo("张三");
        verify(db).insert(c);
    }

    @Test void contactsRequirePersistedProfileAndClientRole() {
        asClient();
        assertThatThrownBy(() -> profiles.contact(null, Map.of("name", "联系人")))
            .isInstanceOf(BusinessException.class).hasMessage("请先完善客户资料");
        CurrentUserContext.set(new AuthenticatedUser(1L, "ADMIN"));
        assertThatThrownBy(profiles::client).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> profiles.saveClient(Map.of("clientName", "企业")))
            .isInstanceOf(BusinessException.class);
    }

    @Test void optionalEmailsAllowEmptyButRejectInvalidAddresses() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            ClientProfile p = new ClientProfile(); p.setUserId(50L); p.setClientType("COMPANY"); p.setClientName("企业");
            p.setPrimaryContactEmail("bad-email");
            assertThat(validator.validate(p)).anyMatch(v -> v.getPropertyPath().toString().equals("primaryContactEmail"));
            p.setPrimaryContactEmail(""); assertThat(validator.validate(p)).isEmpty();
            p.setPrimaryContactEmail("client@example.com"); assertThat(validator.validate(p)).isEmpty();
            ClientContact c = new ClientContact(); c.setClientId(9L); c.setName("联系人"); c.setEmail("bad-email");
            assertThat(validator.validate(c)).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
            c.setEmail(null); assertThat(validator.validate(c)).isEmpty();
        }
    }
}
