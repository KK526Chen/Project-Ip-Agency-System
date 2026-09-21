package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.integration.ExternalSystemAdapter;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class ExternalSyncService {
    private final V2Store db; private final ObjectMapper json; private final List<ExternalSystemAdapter> adapters; private final BusinessEvents events;
    public ExternalSyncService(V2Store db, ObjectMapper json, List<ExternalSystemAdapter> adapters, BusinessEvents events) {
        this.db = db; this.json = json; this.adapters = adapters; this.events = events;
    }
    public PageResult<ExternalSystem> systems(long page, long size) { role("ADMIN"); return db.page(ExternalSystem.class, new QueryWrapper<ExternalSystem>().orderByAsc("id"), page, size); }
    public PageResult<ExternalSyncTask> list(long page, long size, String status) {
        role("ADMIN"); return db.page(ExternalSyncTask.class, new QueryWrapper<ExternalSyncTask>().eq(status != null, "status", status).orderByDesc("id"), page, size);
    }
    @Transactional
    public ExternalSyncTask create(Map<String, Object> body) {
        role("ADMIN"); Long caseId = Input.id(body, "caseId"); db.get(CaseInfo.class, caseId);
        ExternalSystem system = db.one(ExternalSystem.class, new QueryWrapper<ExternalSystem>().eq("system_code", Input.text(body, "systemCode")).eq("status", 1));
        if (system == null) throw new BusinessException("外部系统不存在或已停用");
        String type = Input.text(body, "syncType"); CaseWorkflowService.state(type, "PUSH", "PULL", "STATUS_QUERY");
        ExternalSyncTask task = new ExternalSyncTask(); task.setExternalSystemId(system.getId()); task.setBusinessType("CASE"); task.setBusinessId(caseId);
        task.setSyncType(type); task.setStatus("PENDING"); task.setRetryCount(0); task.setMaxRetryCount(3);
        task.setRequestPayload(encode(Map.of("caseId", caseId, "simulateFailure", Boolean.TRUE.equals(body.get("simulateFailure")))));
        db.insert(task); events.audit("CREATE_SYNC_TASK", "EXTERNAL_SYNC", task.getId()); return task;
    }
    @Transactional
    public ExternalSyncTask execute(Long id, boolean retry) {
        role("ADMIN"); ExternalSyncTask t = db.lock(ExternalSyncTask.class, id);
        CaseWorkflowService.state(t.getStatus(), retry ? "FAILED" : "PENDING");
        if (retry) {
            if (t.getRetryCount() >= t.getMaxRetryCount()) throw new BusinessException("已达到最大重试次数");
            if (t.getNextRetryTime() != null && t.getNextRetryTime().isAfter(LocalDateTime.now())) throw new BusinessException("尚未到重试时间");
            t.setRetryCount(t.getRetryCount() + 1);
        }
        if (!"CASE".equals(t.getBusinessType())) throw new BusinessException("当前适配器仅支持案件同步");
        // Serialize binding creation for this case, including tasks for the same external system.
        db.lock(CaseInfo.class, t.getBusinessId());
        ExternalSystem system = db.get(ExternalSystem.class, t.getExternalSystemId());
        if (!Integer.valueOf(1).equals(system.getStatus())) throw new BusinessException("外部系统已停用");
        ExternalSystemAdapter adapter = adapters.stream().filter(a -> a.systemCode().equals(system.getSystemCode())).findFirst().orElseThrow(() -> new BusinessException("无对应适配器"));
        t.setStatus("PROCESSING"); t.setStartTime(LocalDateTime.now()); db.update(t);
        ExternalSystemAdapter.SyncResult result;
        try {
            result = switch (t.getSyncType()) { case "PUSH" -> adapter.pushCase(t.getBusinessId(), t.getRequestPayload());
                case "PULL" -> adapter.pullDocument(t.getBusinessId(), t.getRequestPayload()); default -> adapter.queryStatus(t.getBusinessId(), t.getRequestPayload()); };
        } catch (Exception e) { result = new ExternalSystemAdapter.SyncResult(false, null, null, "Adapter invocation failed"); }
        t.setStatus(result.success() ? "SUCCESS" : "FAILED"); t.setResponsePayload(encode(result)); t.setFinishTime(LocalDateTime.now());
        t.setLastError(result.success() ? null : result.message()); t.setNextRetryTime(result.success() ? null : LocalDateTime.now().plusSeconds(30)); db.update(t);
        ExternalCaseBinding binding = db.one(ExternalCaseBinding.class, new QueryWrapper<ExternalCaseBinding>().eq("case_id", t.getBusinessId()).eq("external_system_id", system.getId()));
        if (binding == null) { binding = new ExternalCaseBinding(); binding.setCaseId(t.getBusinessId()); binding.setExternalSystemId(system.getId()); }
        if (result.success()) { binding.setExternalCaseId(result.externalCaseId()); binding.setExternalApplicationNo(result.applicationNo()); }
        binding.setSyncStatus(t.getStatus()); binding.setLastSyncTime(LocalDateTime.now()); binding.setLastError(t.getLastError());
        if (binding.getId() == null) db.insert(binding); else db.update(binding);
        events.audit(retry ? "RETRY_SYNC" : "RUN_SYNC", "EXTERNAL_SYNC", id); return t;
    }
    private String encode(Object value) { try { return json.writeValueAsString(value); } catch (Exception e) { throw new BusinessException("同步数据序列化失败"); } }
}
