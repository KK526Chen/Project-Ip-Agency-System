package com.ipagency.service;

import com.ipagency.common.BusinessException;
import com.ipagency.entity.CaseInfo;
import com.ipagency.entity.CaseParty;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseCommissionRulesTest {
    @Test void serviceProductMustMatchCaseType() {
        assertThatCode(() -> CaseCommissionRules.assertServiceMatches("PATENT_APPLICATION", "INVENTION_PATENT")).doesNotThrowAnyException();
        assertThatThrownBy(() -> CaseCommissionRules.assertServiceMatches("TRADEMARK_REGISTRATION", "INVENTION_PATENT"))
            .isInstanceOf(BusinessException.class).hasMessage("所选服务产品与案件类型不匹配");
        assertThatCode(() -> CaseCommissionRules.assertServiceMatches("IP_PROTECTION", "INVALIDATION")).doesNotThrowAnyException();
    }

    @Test void onePrimaryPerPartyType() {
        assertThatThrownBy(() -> CaseCommissionRules.assertPrimaryUnique(List.of(party("APPLICANT", 1), party("APPLICANT", 1))))
            .isInstanceOf(BusinessException.class);
        assertThatCode(() -> CaseCommissionRules.assertPrimaryUnique(List.of(party("APPLICANT", 1), party("INVENTOR", 1)))).doesNotThrowAnyException();
    }

    @Test void submitRequiresApplicantInventorAndNiceClass() {
        CaseInfo patent = info("INVENTION_PATENT", "G06N", 1L);
        assertThatThrownBy(() -> CaseCommissionRules.assertReadyToSubmit(patent, List.of(party("APPLICANT", 1))))
            .isInstanceOf(BusinessException.class).hasMessage("专利申请请填写至少一名发明人");
        assertThatCode(() -> CaseCommissionRules.assertReadyToSubmit(patent, List.of(party("APPLICANT", 1), party("INVENTOR", 1)))).doesNotThrowAnyException();

        CaseInfo design = info("DESIGN_PATENT", "洛迦诺 14-03", 1L);
        assertThatThrownBy(() -> CaseCommissionRules.assertReadyToSubmit(design, List.of(party("APPLICANT", 1))))
            .isInstanceOf(BusinessException.class).hasMessage("外观设计请填写至少一名设计人");

        CaseInfo trademark = info("TRADEMARK", "  ", 2L);
        assertThatThrownBy(() -> CaseCommissionRules.assertReadyToSubmit(trademark, List.of(party("APPLICANT", 1))))
            .isInstanceOf(BusinessException.class).hasMessage("商标案件请填写尼斯分类");

        CaseInfo draft = info("COPYRIGHT", null, null);
        assertThatThrownBy(() -> CaseCommissionRules.assertReadyToSubmit(draft, List.of(party("APPLICANT", 1))))
            .isInstanceOf(BusinessException.class).hasMessage("提交前请选择服务产品");
    }

    private static CaseInfo info(String type, String field, Long serviceId) {
        CaseInfo info = new CaseInfo();
        info.setCaseType(type);
        info.setTechnicalField(field);
        info.setServiceProductId(serviceId);
        return info;
    }

    private static CaseParty party(String type, int primary) {
        CaseParty party = new CaseParty();
        party.setPartyType(type);
        party.setName("测试主体");
        party.setIsPrimary(primary);
        return party;
    }
}
