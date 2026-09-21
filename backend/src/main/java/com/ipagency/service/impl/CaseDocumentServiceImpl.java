package com.ipagency.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ipagency.entity.CaseDocument;
import com.ipagency.mapper.CaseDocumentMapper;
import com.ipagency.service.CaseDocumentService;
import org.springframework.stereotype.Service;
@Service public class CaseDocumentServiceImpl extends ServiceImpl<CaseDocumentMapper, CaseDocument> implements CaseDocumentService { }
