package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChecklistService {
    private final V2Store db; private final CaseAccessServiceImpl access; private final ObjectMapper json;
    public ChecklistService(V2Store db, CaseAccessServiceImpl access, ObjectMapper json) {
        this.db = db; this.access = access; this.json = json;
    }
    public void instantiate(Long caseId, String gateCode) {
        if (db.count(ChecklistInstance.class, new QueryWrapper<ChecklistInstance>().eq("case_id", caseId).eq("gate_code", gateCode)) > 0) return;
        ChecklistTemplate tpl = db.one(ChecklistTemplate.class, new QueryWrapper<ChecklistTemplate>().eq("gate_code", gateCode));
        if (tpl == null) return;
        ChecklistInstance inst = new ChecklistInstance(); inst.setCaseId(caseId); inst.setTemplateId(tpl.getId());
        inst.setGateCode(gateCode); inst.setStatus("OPEN"); db.insert(inst);
        List<String> labels = List.of("申请文件尚未审核通过", "客户尚未确认");
        try { if (tpl.getItemsJson() != null) labels = json.readValue(tpl.getItemsJson(), new TypeReference<>() {}); } catch (Exception ignored) { }
        for (String label : labels) {
            ChecklistItem item = new ChecklistItem(); item.setInstanceId(inst.getId()); item.setItemLabel(label);
            item.setRequiredFlag(1); item.setCheckedFlag(0); item.setMissingMessage(label); db.insert(item);
        }
    }
    public List<ChecklistInstance> list(Long caseId) {
        access.requireView(caseId);
        return db.mapper(ChecklistInstance.class).selectList(new QueryWrapper<ChecklistInstance>().eq("case_id", caseId));
    }
    public List<ChecklistItem> items(Long instanceId) {
        return db.mapper(ChecklistItem.class).selectList(new QueryWrapper<ChecklistItem>().eq("instance_id", instanceId).orderByAsc("id"));
    }
    @Transactional
    public ChecklistItem check(Long itemId, boolean checked) {
        ChecklistItem item = db.lock(ChecklistItem.class, itemId);
        ChecklistInstance inst = db.get(ChecklistInstance.class, item.getInstanceId());
        access.requireManage(inst.getCaseId());
        item.setCheckedFlag(checked ? 1 : 0); db.update(item); return item;
    }
    public void assertGate(Long caseId, String gateCode) {
        instantiate(caseId, gateCode);
        if ("SUBMIT_GATE".equals(gateCode) && !hasApprovedApplication(caseId)) {
            throw gate(List.of("申请文件尚未审核通过"));
        }
        ChecklistInstance inst = db.one(ChecklistInstance.class, new QueryWrapper<ChecklistInstance>().eq("case_id", caseId).eq("gate_code", gateCode));
        if (inst == null) return;
        List<String> missing = new ArrayList<>();
        for (ChecklistItem item : items(inst.getId())) {
            if (Integer.valueOf(1).equals(item.getRequiredFlag()) && !Integer.valueOf(1).equals(item.getCheckedFlag()))
                missing.add(item.getMissingMessage() == null ? item.getItemLabel() : item.getMissingMessage());
        }
        if ("SUBMIT_GATE".equals(gateCode) && !hasApprovedApplication(caseId) && missing.stream().noneMatch(s -> s.contains("申请文件")))
            missing.add(0, "申请文件尚未审核通过");
        if (!missing.isEmpty()) throw gate(missing);
    }
    private boolean hasApprovedApplication(Long caseId) {
        return db.count(CaseDocument.class, new QueryWrapper<CaseDocument>().eq("case_id", caseId).eq("document_type", "APPLICATION").eq("review_status", "APPROVED")) > 0;
    }
    private BusinessException gate(List<String> missing) {
        return new BusinessException("BUSINESS_GATE_FAILED: " + String.join("；", missing), HttpStatus.CONFLICT);
    }
}
