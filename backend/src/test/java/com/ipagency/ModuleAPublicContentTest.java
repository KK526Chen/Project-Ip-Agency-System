package com.ipagency;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ModuleAPublicContentTest {
    private final V2Store db = mock(V2Store.class);
    private final BusinessEvents events = mock(BusinessEvents.class);
    private final PublicContentService content = new PublicContentService(db,
        new Input(new ObjectMapper().findAndRegisterModules()), events);

    @AfterEach void clear() { CurrentUserContext.clear(); }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test void publicAnnouncementListAndDetailShareVisibilityRules() {
        content.list(Announcement.class, false, 1, 10, "政策", "POLICY");
        ArgumentCaptor<QueryWrapper> listQuery = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(db).page(eq(Announcement.class), listQuery.capture(), eq(1L), eq(10L));
        String sql = listQuery.getValue().getSqlSegment();
        assertThat(sql).contains("status =", "target_scope =", "publish_time <=",
            "start_date IS NULL", "end_date IS NULL", "title LIKE", "announcement_type =",
            "ORDER BY is_top DESC,publish_time DESC,id DESC");
        assertThat(listQuery.getValue().getParamNameValuePairs().values()).contains("ALL", "POLICY", 1);
        assertThatThrownBy(() -> content.detail(Announcement.class, 7L))
            .isInstanceOf(BusinessException.class).hasMessage("内容不存在");
        ArgumentCaptor<QueryWrapper> detailQuery = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(db).one(eq(Announcement.class), detailQuery.capture());
        assertThat(detailQuery.getValue().getSqlSegment()).contains("target_scope =", "publish_time <=", "id =");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test void blankCategoryDoesNotFilterOutAllServices() {
        content.list(ServiceProduct.class, false, 1, 10, null, "");
        ArgumentCaptor<QueryWrapper> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(db).page(eq(ServiceProduct.class), query.capture(), eq(1L), eq(10L));
        assertThat(query.getValue().getSqlSegment()).contains("status =").doesNotContain("service_type =");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test void unpublishedSuccessCasesAreExcluded() {
        content.list(SuccessCase.class, false, 1, 10, null, "PATENT_APPLICATION");
        ArgumentCaptor<QueryWrapper> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(db).page(eq(SuccessCase.class), query.capture(), eq(1L), eq(10L));
        assertThat(query.getValue().getSqlSegment()).contains("publish_status =", "service_type =");
    }

    @Test void newlyCreatedContentAuditContainsGeneratedId() {
        CurrentUserContext.set(new AuthenticatedUser(1L, "ADMIN"));
        doAnswer(call -> { ((ServiceProduct) call.getArgument(0)).setId(42L); return call.getArgument(0); })
            .when(db).insert(any(ServiceProduct.class));
        ServiceProduct saved = content.save(ServiceProduct.class, null,
            Map.of("serviceNo", "S42", "serviceName", "专利申请", "serviceType", "PATENT_APPLICATION"));
        assertThat(saved.getId()).isEqualTo(42L);
        verify(events).audit("SAVE_CONTENT", "ServiceProduct", 42L);
    }

    @Test void onlyAdminCanManageContentAndOwnershipFieldsAreRejected() {
        CurrentUserContext.set(new AuthenticatedUser(2L, "CLIENT"));
        assertThatThrownBy(() -> content.save(ServiceProduct.class, null, Map.of()))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> content.delete(ServiceProduct.class, 1L))
            .isInstanceOf(BusinessException.class);
        verifyNoInteractions(db, events);
        CurrentUserContext.set(new AuthenticatedUser(1L, "ADMIN"));
        assertThatThrownBy(() -> content.save(Announcement.class, null, Map.of("publisherUserId", 99L)))
            .isInstanceOf(BusinessException.class).hasMessageContaining("不允许修改字段");
    }

    @Test void invalidAnnouncementDatesAreRejectedBeforePersistence() {
        CurrentUserContext.set(new AuthenticatedUser(1L, "ADMIN"));
        assertThatThrownBy(() -> content.save(Announcement.class, null,
            Map.of("startDate", "2026-10-02", "endDate", "2026-10-01")))
            .isInstanceOf(BusinessException.class).hasMessage("公告日期范围无效");
        verify(db, never()).insert(any());
    }

    @Test void contentEnumsAndFeesAreValidated() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            SuccessCase success = new SuccessCase();
            success.setCaseNo("SC1"); success.setCaseName("示例"); success.setServiceType("INVALID");
            assertThat(validator.validate(success)).anyMatch(v -> v.getPropertyPath().toString().equals("serviceType"));
            Announcement announcement = new Announcement();
            announcement.setAnnouncementNo("A1"); announcement.setTitle("公告");
            announcement.setAnnouncementType("BUSINESS"); announcement.setContent("正文");
            announcement.setTargetScope("INVALID");
            assertThat(validator.validate(announcement)).anyMatch(v -> v.getPropertyPath().toString().equals("targetScope"));
            ServiceProduct service = new ServiceProduct();
            service.setServiceNo("S1"); service.setServiceName("服务"); service.setServiceType("PATENT_APPLICATION");
            service.setAgencyFee(new BigDecimal("-0.01"));
            assertThat(validator.validate(service)).anyMatch(v -> v.getPropertyPath().toString().equals("agencyFee"));
            service.setAgencyFee(new BigDecimal("1.001"));
            assertThat(validator.validate(service)).anyMatch(v -> v.getPropertyPath().toString().equals("agencyFee"));
            service.setAgencyFee(new BigDecimal("0.00"));
            assertThat(validator.validate(service)).isEmpty();
        }
    }
}
