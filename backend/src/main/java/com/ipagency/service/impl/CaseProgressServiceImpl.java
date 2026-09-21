package com.ipagency.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ipagency.entity.CaseProgress;
import com.ipagency.mapper.CaseProgressMapper;
import com.ipagency.service.CaseProgressService;
import org.springframework.stereotype.Service;
@Service public class CaseProgressServiceImpl extends ServiceImpl<CaseProgressMapper, CaseProgress> implements CaseProgressService { }
