package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class DocumentService {
    private final V2Store db; private final CaseAccessServiceImpl access; private final Input input;
    private final BusinessEvents events; private final CaseWorkflowService workflow; private final DeadlineService deadlines; private final ObjectMapper json;
    public DocumentService(V2Store db, CaseAccessServiceImpl access, Input input, BusinessEvents events, CaseWorkflowService workflow, DeadlineService deadlines, ObjectMapper json) {
        this.db = db; this.access = access; this.input = input; this.events = events; this.workflow = workflow; this.deadlines = deadlines; this.json = json;
    }
    public PageResult<CaseDocument> list(Long caseId, String type, String review, long page, long size) {
        if (caseId != null) access.requireView(caseId);
        return db.page(CaseDocument.class, access.scope(new QueryWrapper<CaseDocument>(), "case_id", false).eq(caseId != null, "case_id", caseId)
            .eq(type != null, "document_type", type).eq(review != null, "review_status", review).orderByDesc("create_time", "id"), page, size);
    }
    @Transactional
    public CaseDocument ocr(Long id, Map<String, Object> body) {
        CaseDocument d = db.lock(CaseDocument.class, id); access.requireManage(d.getCaseId());
        input.apply(body, d, "ocrText ocrExtractedJson officialIssueDate officialDeadline feeAmountExtracted");
        if (d.getOcrExtractedJson() != null) try { json.readTree(d.getOcrExtractedJson()); } catch (Exception e) { throw new BusinessException("OCR 结果必须是合法 JSON 字符串"); }
        if (body.isEmpty()) throw new BusinessException("请提交人工确认的提取字段");
        d.setOcrStatus("MANUAL"); db.update(d);
        if ("OFFICIAL".equals(d.getDocumentType()) && d.getOfficialDeadline() != null) {
            DeadlineTask task = db.one(DeadlineTask.class, new QueryWrapper<DeadlineTask>().eq("document_id", id).eq("deadline_type", "OFFICIAL_RESPONSE"));
            if (task == null || !"COMPLETED".equals(task.getStatus())) {
                Map<String, Object> fields = new LinkedHashMap<>(); fields.put("caseId", d.getCaseId()); fields.put("documentId", id);
                fields.put("deadlineType", "OFFICIAL_RESPONSE"); fields.put("taskName", "官文答复: " + d.getDocumentName());
                fields.put("officialDeadline", d.getOfficialDeadline().atTime(23, 59, 59));
                deadlines.save(task == null ? null : task.getId(), fields);
            }
        }
        events.audit("CONFIRM_OCR", "DOCUMENT", id); return d;
    }
    @Transactional
    public ReviewRecord review(Map<String, Object> body) {
        role("ADMIN"); String target = Input.text(body, "targetType"); Long id = Input.id(body, "targetId");
        if ("CASE".equals(target)) {
            if (body.containsKey("caseId") && !Objects.equals(Input.id(body, "caseId"), id)) throw new BusinessException("审核对象不属于该案件");
            return workflow.review(id, body);
        }
        CaseWorkflowService.state(target, "DOCUMENT", "SUPPLEMENT");
        CaseDocument d = db.lock(CaseDocument.class, id);
        CaseWorkflowService.state(d.getReviewStatus(), "PENDING");
        if (body.containsKey("caseId") && !Objects.equals(Input.id(body, "caseId"), d.getCaseId())) throw new BusinessException("审核对象不属于该案件");
        if ("SUPPLEMENT".equals(target) && !"SUPPLEMENT".equals(d.getDocumentType())) throw new BusinessException("审核对象不是补充材料");
        String result = Input.text(body, "reviewResult");
        CaseWorkflowService.state(result, "APPROVED", "MINOR_REVISION", "MAJOR_REVISION", "REJECTED", "RESUBMIT_REQUIRED");
        ReviewRecord r = workflow.record(d.getCaseId(), target, id, "SUPPLEMENT".equals(target) ? "MATERIAL" : "INTERNAL_QUALITY", result, (String)body.get("reviewComment"));
        d.setReviewStatus(result); db.update(d); events.caseEvent(d.getCaseId(), "MATERIAL_REVIEW", "文件审核结果: " + result);
        events.audit("REVIEW_DOCUMENT", "DOCUMENT", id); return r;
    }
}
