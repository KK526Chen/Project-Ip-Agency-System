package com.ipagency.service;

import com.ipagency.common.BusinessException;
import com.ipagency.entity.CaseInfo;
import com.ipagency.entity.CaseParty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Rules for the client commission on case_info, case_party and case_priority. */
public final class CaseCommissionRules {
    private static final Set<String> PATENT_WITH_INVENTOR = Set.of("INVENTION_PATENT", "UTILITY_MODEL");

    private CaseCommissionRules() {
    }

    public static void assertServiceMatches(String serviceType, String caseType) {
        boolean matches = switch (serviceType) {
            case "PATENT_APPLICATION" -> Set.of("INVENTION_PATENT", "UTILITY_MODEL", "DESIGN_PATENT").contains(caseType);
            case "TRADEMARK_REGISTRATION" -> "TRADEMARK".equals(caseType);
            case "COPYRIGHT_REGISTRATION" -> "COPYRIGHT".equals(caseType);
            default -> true;
        };
        if (!matches) throw new BusinessException("所选服务产品与案件类型不匹配");
    }

    public static void assertPrimaryUnique(List<CaseParty> parties) {
        Map<String, Integer> primaries = new HashMap<>();
        for (CaseParty party : parties) {
            if (Integer.valueOf(1).equals(party.getIsPrimary())) primaries.merge(party.getPartyType(), 1, Integer::sum);
        }
        if (primaries.values().stream().anyMatch(count -> count > 1)) throw new BusinessException("同一当事人类型只能指定一名第一主体");
    }

    public static void assertReadyToSubmit(CaseInfo info, List<CaseParty> parties) {
        if (info.getServiceProductId() == null) throw new BusinessException("提交前请选择服务产品");
        if (count(parties, "APPLICANT") == 0) throw new BusinessException("提交前请填写至少一名申请人");
        if (PATENT_WITH_INVENTOR.contains(info.getCaseType()) && count(parties, "INVENTOR") == 0)
            throw new BusinessException("专利申请请填写至少一名发明人");
        if ("DESIGN_PATENT".equals(info.getCaseType()) && count(parties, "DESIGNER") == 0)
            throw new BusinessException("外观设计请填写至少一名设计人");
        if ("TRADEMARK".equals(info.getCaseType()) && (info.getTechnicalField() == null || info.getTechnicalField().isBlank()))
            throw new BusinessException("商标案件请填写尼斯分类");
        assertPrimaryUnique(parties);
    }

    private static long count(List<CaseParty> parties, String type) {
        return parties.stream().filter(party -> type.equals(party.getPartyType())).count();
    }
}
