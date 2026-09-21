package com.ipagency.service;

import com.ipagency.common.AuthenticatedUser;

public interface CaseAccessService {
    boolean canViewCase(AuthenticatedUser user, Long caseId);
    boolean canManageCase(AuthenticatedUser user, Long caseId);
    void requireView(Long caseId);
    void requireManage(Long caseId);
}
