package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentVersionService {
    static final ThreadLocal<Boolean> SKIP_AUTO_SERIES = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private final V2Store db; private final CaseAccessServiceImpl access; private final DocumentStorageService storage;
    private final BusinessEvents events; private final DomainEventPublisher publisher;
    public DocumentVersionService(V2Store db, CaseAccessServiceImpl access, DocumentStorageService storage, BusinessEvents events, DomainEventPublisher publisher) {
        this.db = db; this.access = access; this.storage = storage; this.events = events; this.publisher = publisher;
    }
    public void ensureSeries(CaseDocument d) {
        if (Boolean.TRUE.equals(SKIP_AUTO_SERIES.get())) return;
        DocumentVersion existing = db.one(DocumentVersion.class, new QueryWrapper<DocumentVersion>().eq("document_id", d.getId()));
        if (existing != null) return;
        DocumentSeries s = new DocumentSeries(); s.setCaseId(d.getCaseId()); s.setDocumentType(d.getDocumentType());
        s.setLogicalName(d.getDocumentName()); s.setStatus("ACTIVE"); db.insert(s);
        DocumentVersion v = new DocumentVersion(); v.setSeriesId(s.getId()); v.setDocumentId(d.getId()); v.setVersionNo(1);
        v.setVersionStatus("CURRENT"); db.insert(v);
        s.setCurrentVersionId(d.getId()); db.update(s);
    }
    public List<DocumentVersion> versions(Long documentId) {
        CaseDocument d = db.get(CaseDocument.class, documentId); access.requireView(d.getCaseId());
        DocumentVersion v = db.one(DocumentVersion.class, new QueryWrapper<DocumentVersion>().eq("document_id", documentId));
        if (v == null) { ensureSeries(d); v = db.one(DocumentVersion.class, new QueryWrapper<DocumentVersion>().eq("document_id", documentId)); }
        return db.mapper(DocumentVersion.class).selectList(new QueryWrapper<DocumentVersion>().eq("series_id", v.getSeriesId()).orderByAsc("version_no"));
    }
    @Transactional
    public CaseDocument createVersion(Long documentId, MultipartFile file, String remark) {
        CaseDocument old = db.lock(CaseDocument.class, documentId); access.requireManage(old.getCaseId());
        ensureSeries(old);
        DocumentVersion current = db.one(DocumentVersion.class, new QueryWrapper<DocumentVersion>().eq("document_id", documentId));
        DocumentSeries series = db.lock(DocumentSeries.class, current.getSeriesId());
        if (Set.of("CLIENT_APPROVED", "FILED", "ARCHIVED").contains(series.getStatus()))
            throw new BusinessException("DOCUMENT_VERSION_CONFLICT: 已归档文件需先提交变更申请", HttpStatus.CONFLICT);
        String oldPath = old.getFilePath();
        CaseDocument neu;
        SKIP_AUTO_SERIES.set(true);
        try { neu = storage.upload(file, old.getCaseId(), old.getDocumentType(), old.getStageId(), remark); }
        finally { SKIP_AUTO_SERIES.remove(); }
        DocumentVersion v2 = new DocumentVersion(); v2.setSeriesId(series.getId()); v2.setDocumentId(neu.getId());
        v2.setParentDocumentId(old.getId());
        Integer max = db.mapper(DocumentVersion.class).selectList(new QueryWrapper<DocumentVersion>().eq("series_id", series.getId()))
            .stream().map(DocumentVersion::getVersionNo).max(Integer::compareTo).orElse(1);
        v2.setVersionNo(max + 1); v2.setVersionStatus("CURRENT"); db.insert(v2);
        series.setCurrentVersionId(neu.getId()); db.update(series);
        CaseDocument persistedOld = db.get(CaseDocument.class, old.getId());
        if (!Objects.equals(oldPath, persistedOld.getFilePath())) throw new BusinessException("不允许覆盖历史版本文件");
        events.audit("CREATE_DOCUMENT_VERSION", "DOCUMENT", neu.getId());
        publisher.publish("DOCUMENT_VERSION_CREATED", old.getCaseId(), neu.getId(), neu.getDocumentName());
        return neu;
    }
    public void markApproved(CaseDocument d) {
        DocumentVersion v = db.one(DocumentVersion.class, new QueryWrapper<DocumentVersion>().eq("document_id", d.getId()));
        if (v == null) return;
        DocumentSeries s = db.get(DocumentSeries.class, v.getSeriesId());
        s.setApprovedVersionId(d.getId()); db.update(s);
    }
    @Transactional
    public ChangeRequest requestChange(Long documentId, Map<String, Object> body) {
        CaseDocument d = db.get(CaseDocument.class, documentId); access.requireManage(d.getCaseId());
        ensureSeries(d);
        DocumentVersion v = db.one(DocumentVersion.class, new QueryWrapper<DocumentVersion>().eq("document_id", documentId));
        ChangeRequest cr = new ChangeRequest(); cr.setSeriesId(v.getSeriesId()); cr.setSourceVersionId(v.getId());
        cr.setRequesterUserId(CurrentUserContext.require().userId()); cr.setStatus("PENDING");
        cr.setReason((String) body.get("reason")); cr.setImpact((String) body.get("impact"));
        db.insert(cr); events.audit("CREATE_CHANGE_REQUEST", "DOCUMENT", documentId); return cr;
    }
}
