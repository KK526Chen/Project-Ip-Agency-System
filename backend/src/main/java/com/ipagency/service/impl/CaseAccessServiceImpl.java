package com.ipagency.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ipagency.common.AuthenticatedUser;
import com.ipagency.common.BusinessException;
import com.ipagency.common.CurrentUserContext;
import com.ipagency.entity.CaseInfo;
import com.ipagency.entity.CaseMember;
import com.ipagency.mapper.CaseInfoMapper;
import com.ipagency.mapper.CaseMemberMapper;
import com.ipagency.service.CaseAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CaseAccessServiceImpl implements CaseAccessService {
    private final CaseInfoMapper caseInfoMapper;
    private final CaseMemberMapper caseMemberMapper;

    public CaseAccessServiceImpl(CaseInfoMapper caseInfoMapper, CaseMemberMapper caseMemberMapper) {
        this.caseInfoMapper = caseInfoMapper;
        this.caseMemberMapper = caseMemberMapper;
    }

    @Override
    public boolean canViewCase(AuthenticatedUser user, Long caseId) {
        CaseInfo caseInfo = caseInfoMapper.selectById(caseId);
        if (caseInfo == null) return false;
        if (user.isAdmin() || user.userId().equals(caseInfo.getPrincipalId())) return true;
        return caseMemberMapper.selectCount(Wrappers.<CaseMember>lambdaQuery()
                .eq(CaseMember::getCaseId, caseId).eq(CaseMember::getUserId, user.userId())) > 0;
    }

    @Override
    public boolean canManageCase(AuthenticatedUser user, Long caseId) {
        CaseInfo caseInfo = caseInfoMapper.selectById(caseId);
        return caseInfo != null && (user.isAdmin()
                || ("AGENT".equals(user.role()) && user.userId().equals(caseInfo.getPrincipalId())));
    }

    @Override
    public void requireView(Long caseId) {
        if (!canViewCase(CurrentUserContext.require(), caseId)) {
            throw new BusinessException("无权访问该案件", HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public void requireManage(Long caseId) {
        if (!canManageCase(CurrentUserContext.require(), caseId)) {
            throw new BusinessException("无权管理该案件", HttpStatus.FORBIDDEN);
        }
    }
}
