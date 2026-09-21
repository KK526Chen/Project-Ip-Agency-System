package com.ipagency.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ipagency.entity.CaseTask;
import com.ipagency.mapper.CaseTaskMapper;
import com.ipagency.service.CaseTaskService;
import org.springframework.stereotype.Service;
@Service public class CaseTaskServiceImpl extends ServiceImpl<CaseTaskMapper, CaseTask> implements CaseTaskService { }
