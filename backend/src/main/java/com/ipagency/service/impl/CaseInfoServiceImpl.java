package com.ipagency.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ipagency.entity.CaseInfo;
import com.ipagency.mapper.CaseInfoMapper;
import com.ipagency.service.CaseInfoService;
import org.springframework.stereotype.Service;
@Service public class CaseInfoServiceImpl extends ServiceImpl<CaseInfoMapper, CaseInfo> implements CaseInfoService { }
