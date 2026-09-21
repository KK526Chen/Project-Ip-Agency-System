package com.ipagency.service.impl;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageServiceImpl implements DocumentStorageService {
    private static final Set<String> TYPES = Set.of("TECHNICAL_DISCLOSURE", "TRADEMARK_IMAGE", "APPLICATION", "OFFICE_ACTION_RESPONSE", "OFFICIAL", "SUPPLEMENT", "INTERNAL", "CERTIFICATE", "OTHER");
    private final Path root;
    private final CaseAccessServiceImpl access;
    private final V2Store db;
    private final BusinessEvents events;
    private final CaseWorkflowService workflow;
    public DocumentStorageServiceImpl(@Value("${app.file.upload-dir}") String directory, CaseAccessServiceImpl access, V2Store db, BusinessEvents events, CaseWorkflowService workflow) {
        this.root = Path.of(directory).toAbsolutePath().normalize(); this.access = access; this.db = db; this.events = events; this.workflow = workflow;
    }
    @Override @Transactional
    public CaseDocument upload(MultipartFile file, Long caseId, String type, Long stageId, String remark) {
        access.requireView(caseId);
        CaseInfo c = db.lock(CaseInfo.class, caseId);
        String role = CurrentUserContext.require().role();
        if (!TYPES.contains(type)) throw new BusinessException("文件类型不合法");
        if ("CLIENT".equals(role)) {
            if (!Set.of("TECHNICAL_DISCLOSURE", "TRADEMARK_IMAGE", "SUPPLEMENT", "OTHER").contains(type)) CaseAccessServiceImpl.denied();
            if (Set.of("CLOSED", "WITHDRAWN", "EXPIRED").contains(c.getStatus())) throw new BusinessException("案件已结束");
        }
        if (stageId != null && !Objects.equals(db.get(CaseStage.class, stageId).getCaseId(), caseId)) throw new BusinessException("阶段不属于该案件");
        if (file == null || file.isEmpty()) throw new BusinessException("文件不能为空");
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file").replace((char)92, '/');
        original = original.substring(original.lastIndexOf('/') + 1).replace('\r', '_').replace('\n', '_');
        if (original.isBlank() || original.length() > 300) throw new BusinessException("文件名无效");
        Path target = root.resolve("case-" + caseId).resolve(type.toLowerCase(Locale.ROOT)).resolve(UUID.randomUUID().toString());
        try {
            Files.createDirectories(target.getParent());
            if (!target.getParent().toRealPath().startsWith(root.toRealPath())) throw new BusinessException("文件路径不合法");
            try (InputStream stream = file.getInputStream()) { Files.copy(stream, target); }
            if (TransactionSynchronizationManager.isSynchronizationActive()) TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) { if (status != STATUS_COMMITTED) cleanup(target); }
            });
            CaseDocument d = new CaseDocument(); d.setCaseId(caseId); d.setStageId(stageId); d.setDocumentNo("DOC-" + UUID.randomUUID());
            d.setDocumentName(original); d.setOriginalFileName(original); d.setDocumentType(type); d.setSourceType("OFFICIAL".equals(type) ? "OFFICIAL" : role);
            d.setFilePath(root.relativize(target).toString().replace((char)92, '/')); d.setFileSize(file.getSize());
            d.setMimeType(file.getContentType() == null ? "application/octet-stream" : file.getContentType()); d.setUploaderUserId(CurrentUserContext.require().userId());
            d.setRemark(remark); d.setOcrStatus("NOT_STARTED");
            d.setReviewStatus(Set.of("APPLICATION", "OFFICE_ACTION_RESPONSE", "SUPPLEMENT").contains(type) ? "PENDING" : "NOT_REQUIRED");
            if ("OFFICIAL".equals(type)) {
                d.setReceiveTime(LocalDateTime.now());
                if (stageId == null) d.setStageId(workflow.stageEvent(caseId, "OFFICE_ACTION", "官文到达", "IN_PROGRESS").getId());
            }
            db.insert(d);
            if ("OFFICIAL".equals(type)) events.caseEvent(caseId, "OFFICIAL_DOCUMENT", "新官文到达: " + original);
            events.audit("UPLOAD_DOCUMENT", "DOCUMENT", d.getId()); return d;
        } catch (IOException e) { cleanup(target); throw new BusinessException("文件保存失败"); }
        catch (RuntimeException e) { cleanup(target); throw e; }
    }
    private static void cleanup(Path path) { try { Files.deleteIfExists(path); } catch (IOException e) { org.slf4j.LoggerFactory.getLogger(DocumentStorageServiceImpl.class).warn("Could not remove rolled-back upload"); } }
    @Override
    public DownloadedDocument download(Long id) {
        CaseDocument d = db.get(CaseDocument.class, id); access.requireView(d.getCaseId());
        try {
            Path saved = Path.of(d.getFilePath()); Path path = (saved.isAbsolute() ? saved : root.resolve(saved)).normalize();
            if (!path.startsWith(root) || !Files.isRegularFile(path) || !path.toRealPath().startsWith(root.toRealPath())) throw new BusinessException("文件不存在", HttpStatus.NOT_FOUND);
            events.audit("DOWNLOAD_DOCUMENT", "DOCUMENT", id);
            return new DownloadedDocument(d, new FileSystemResource(path), "application/octet-stream");
        } catch (IOException | InvalidPathException e) { throw new BusinessException("文件不存在", HttpStatus.NOT_FOUND); }
    }
}
