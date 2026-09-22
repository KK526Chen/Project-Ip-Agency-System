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
    private final org.springframework.context.ApplicationContext context;
    private final DomainEventPublisher publisher;
    public DocumentStorageServiceImpl(@Value("${app.file.upload-dir}") String directory, CaseAccessServiceImpl access, V2Store db, BusinessEvents events, CaseWorkflowService workflow,
            org.springframework.context.ApplicationContext context, DomainEventPublisher publisher) {
        this.root = Path.of(directory).toAbsolutePath().normalize(); this.access = access; this.db = db; this.events = events; this.workflow = workflow;
        this.context = context; this.publisher = publisher;
    }
    @Override @Transactional
    public CaseDocument upload(MultipartFile file, Long caseId, String type, Long stageId, String remark) {
        access.requireView(caseId);
        CaseInfo c = db.lock(CaseInfo.class, caseId);
        String role = CurrentUserContext.require().role();
        if (!TYPES.contains(type)) throw new BusinessException("文件类型不合法");
        if ("CLIENT".equals(role)) {
            if (!Set.of("TECHNICAL_DISCLOSURE", "TRADEMARK_IMAGE", "SUPPLEMENT", "OTHER").contains(type))
                throw new BusinessException("DOCUMENT_TYPE_FORBIDDEN: 客户不能上传该类型文件", HttpStatus.FORBIDDEN);
            if (Set.of("CLOSED", "WITHDRAWN", "EXPIRED").contains(c.getStatus())) throw new BusinessException("案件已结束");
        } else if ("AGENT".equals(role)) {
            if (!Set.of("APPLICATION", "OFFICE_ACTION_RESPONSE", "INTERNAL", "SUPPLEMENT", "OTHER").contains(type))
                throw new BusinessException("DOCUMENT_TYPE_FORBIDDEN: 代理人不能上传该类型文件", HttpStatus.FORBIDDEN);
        } else if ("ADMIN".equals(role)) {
            if (!TYPES.contains(type)) throw new BusinessException("DOCUMENT_TYPE_FORBIDDEN: 文件类型不合法");
        } else CaseAccessServiceImpl.denied();
        if (stageId != null && !Objects.equals(db.get(CaseStage.class, stageId).getCaseId(), caseId)) throw new BusinessException("阶段不属于该案件");
        if (file == null || file.isEmpty()) throw new BusinessException("文件不能为空");
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file").replace((char)92, '/');
        original = original.substring(original.lastIndexOf('/') + 1).replace('\r', '_').replace('\n', '_');
        if (original.isBlank() || original.length() > 300) throw new BusinessException("文件名无效");
        Path target = root.resolve("case-" + caseId).resolve(type.toLowerCase(Locale.ROOT)).resolve(UUID.randomUUID().toString());
        try {
            Files.createDirectories(target.getParent());
            if (!insideRoot(target.getParent())) throw new BusinessException("文件路径不合法");
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
            try { context.getBean(DocumentVersionService.class).ensureSeries(d); } catch (Exception ignored) { }
            if ("OFFICIAL".equals(type)) {
                try { context.getBean(OcrPipelineService.class).createPending(d); } catch (Exception ignored) { }
                events.caseEvent(caseId, "OFFICIAL_DOCUMENT", "新官文到达: " + original);
                publisher.publish("OFFICIAL_DOCUMENT_RECEIVED", caseId, d.getId(), original);
            }
            events.audit("UPLOAD_DOCUMENT", "DOCUMENT", d.getId());
            publisher.publish("DOCUMENT_UPLOADED", caseId, d.getId(), original);
            return d;
        } catch (IOException e) { cleanup(target); throw new BusinessException("文件保存失败"); }
        catch (RuntimeException e) { cleanup(target); throw e; }
    }
    private static void cleanup(Path path) { try { Files.deleteIfExists(path); } catch (IOException e) { org.slf4j.LoggerFactory.getLogger(DocumentStorageServiceImpl.class).warn("Could not remove rolled-back upload"); } }
    private boolean insideRoot(Path candidate) {
        Path file = candidate.toAbsolutePath().normalize();
        Path base = root.toAbsolutePath().normalize();
        if (file.startsWith(base)) return true;
        String fs = file.toString().replace('\\', '/');
        String bs = base.toString().replace('\\', '/');
        if (!bs.endsWith("/")) bs = bs + "/";
        return fs.regionMatches(true, 0, bs, 0, bs.length()) || fs.equalsIgnoreCase(bs.substring(0, bs.length() - 1));
    }
    private Path resolveStored(String stored) {
        if (stored == null || stored.isBlank()) throw new BusinessException("文件不存在", HttpStatus.NOT_FOUND);
        Path saved = Path.of(stored.replace('\\', '/'));
        List<Path> candidates = new ArrayList<>();
        if (saved.isAbsolute()) candidates.add(saved.normalize());
        else {
            candidates.add(root.resolve(saved).normalize());
            String stripped = stored.replace('\\', '/');
            if (stripped.startsWith("uploads/")) candidates.add(root.resolve(stripped.substring("uploads/".length())).normalize());
            if (root.getParent() != null) candidates.add(root.getParent().resolve(saved).normalize());
        }
        for (Path path : candidates) {
            if (insideRoot(path) && Files.isRegularFile(path)) return path;
        }
        throw new BusinessException("文件不存在", HttpStatus.NOT_FOUND);
    }
    @Override
    public DownloadedDocument download(Long id) {
        CaseDocument d = db.get(CaseDocument.class, id); access.requireView(d.getCaseId());
        Path path = resolveStored(d.getFilePath());
        events.audit("DOWNLOAD_DOCUMENT", "DOCUMENT", id);
        return new DownloadedDocument(d, new FileSystemResource(path), "application/octet-stream");
    }
}
