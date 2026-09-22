package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OcrPipelineService {
    private final V2Store db; private final CaseAccessServiceImpl access; private final DocumentService documents;
    private final BusinessEvents events;
    public OcrPipelineService(V2Store db, CaseAccessServiceImpl access, DocumentService documents, BusinessEvents events) {
        this.db = db; this.access = access; this.documents = documents; this.events = events;
    }
    public void createPending(CaseDocument d) {
        if (!"OFFICIAL".equals(d.getDocumentType())) return;
        OcrJob job = new OcrJob(); job.setDocumentId(d.getId()); job.setCaseId(d.getCaseId());
        job.setEngine("MOCK"); job.setEngineVersion("v1"); job.setStatus("PENDING"); job.setRetryCount(0);
        db.insert(job);
        extract(job, d);
    }
    private void extract(OcrJob job, CaseDocument d) {
        job.setStatus("PROCESSING"); job.setStartedAt(LocalDateTime.now()); db.update(job);
        job.setRawText("MOCK-OCR " + d.getDocumentName());
        job.setFinishedAt(LocalDateTime.now());
        BigDecimal confidence = d.getDocumentName() != null && d.getDocumentName().toLowerCase(Locale.ROOT).contains("auto")
            ? new BigDecimal("0.98") : new BigDecimal("0.80");
        addField(job.getId(), "DOCUMENT_NAME", d.getDocumentName(), confidence);
        addField(job.getId(), "OFFICIAL_DEADLINE", LocalDate.now().plusDays(30).toString(), confidence);
        job.setStatus(confidence.compareTo(new BigDecimal("0.95")) >= 0 ? "EXTRACTED" : "NEEDS_REVIEW");
        db.update(job);
    }
    private void addField(Long jobId, String name, String value, BigDecimal confidence) {
        OcrExtractedField f = new OcrExtractedField(); f.setJobId(jobId); f.setFieldName(name);
        f.setRawValue(value); f.setParsedValue(value); f.setConfidence(confidence); db.insert(f);
    }
    public PageResult<OcrJob> list(Long caseId, long page, long size) {
        if (caseId != null) access.requireView(caseId);
        return db.page(OcrJob.class, access.scope(new QueryWrapper<OcrJob>(), "case_id", false)
            .eq(caseId != null, "case_id", caseId).orderByDesc("id"), page, size);
    }
    @Transactional
    public OcrJob retry(Long id) {
        OcrJob job = db.lock(OcrJob.class, id); access.requireManage(job.getCaseId());
        job.setRetryCount(job.getRetryCount() == null ? 1 : job.getRetryCount() + 1);
        job.setErrorMessage(null);
        extract(job, db.get(CaseDocument.class, job.getDocumentId()));
        events.audit("RETRY_OCR_JOB", "OCR", id); return job;
    }
    @Transactional
    public CaseDocument confirm(Long jobId, Map<String, Object> body) {
        OcrJob job = db.lock(OcrJob.class, jobId); access.requireManage(job.getCaseId());
        Map<String, Object> fields = new LinkedHashMap<>(body);
        if (!fields.containsKey("officialDeadline")) {
            OcrExtractedField deadline = db.one(OcrExtractedField.class, new QueryWrapper<OcrExtractedField>().eq("job_id", jobId).eq("field_name", "OFFICIAL_DEADLINE"));
            if (deadline != null && deadline.getParsedValue() != null) fields.put("officialDeadline", deadline.getParsedValue());
        }
        if (fields.isEmpty()) fields.put("ocrText", job.getRawText() == null ? "{}" : job.getRawText());
        CaseDocument d = documents.ocr(job.getDocumentId(), fields);
        job.setStatus("CONFIRMED"); db.update(job);
        events.audit("CONFIRM_OCR_JOB", "OCR", jobId); return d;
    }
    public List<OcrExtractedField> fields(Long jobId) {
        OcrJob job = db.get(OcrJob.class, jobId); access.requireView(job.getCaseId());
        return db.mapper(OcrExtractedField.class).selectList(new QueryWrapper<OcrExtractedField>().eq("job_id", jobId));
    }
}
