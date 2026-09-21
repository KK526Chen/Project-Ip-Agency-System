package com.ipagency.service.impl;

import com.ipagency.common.BusinessException;
import com.ipagency.common.CurrentUserContext;
import com.ipagency.entity.CaseDocument;
import com.ipagency.mapper.CaseDocumentMapper;
import com.ipagency.service.CaseAccessService;
import com.ipagency.service.DocumentStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageServiceImpl implements DocumentStorageService {
    private static final Set<String> TYPES = Set.of("CLIENT", "APPLICATION", "OFFICIAL", "INTERNAL", "OTHER");
    private final Path uploadRoot;
    private final CaseAccessService accessService;
    private final CaseDocumentMapper documentMapper;

    public DocumentStorageServiceImpl(@Value("${app.file.upload-dir}") String uploadDir,
                                      CaseAccessService accessService,
                                      CaseDocumentMapper documentMapper) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        this.accessService = accessService;
        this.documentMapper = documentMapper;
    }

    @Override
    @Transactional
    public CaseDocument upload(MultipartFile file, Long caseId, String documentType, String remark) {
        accessService.requireView(caseId);
        if (file == null || file.isEmpty()) throw new BusinessException("上传文件不能为空");
        if (!TYPES.contains(documentType)) throw new BusinessException("文档类型不合法");
        String originalName = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String suffix = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        Path target = uploadRoot.resolve(UUID.randomUUID() + suffix).normalize();
        if (!target.startsWith(uploadRoot)) throw new BusinessException("文件路径不合法");
        try {
            Files.createDirectories(uploadRoot);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            CaseDocument document = new CaseDocument();
            document.setCaseId(caseId);
            document.setDocumentName(originalName);
            document.setDocumentType(documentType);
            document.setFilePath(target.toString());
            document.setUploaderId(CurrentUserContext.require().userId());
            document.setRemark(remark);
            document.setUploadTime(LocalDateTime.now());
            documentMapper.insert(document);
            return document;
        } catch (Exception exception) {
            try { Files.deleteIfExists(target); } catch (IOException ignored) { }
            if (exception instanceof BusinessException businessException) throw businessException;
            throw new BusinessException("文件保存失败");
        }
    }

    @Override
    public DownloadedDocument download(Long documentId) {
        CaseDocument document = documentMapper.selectById(documentId);
        if (document == null) throw new BusinessException("文档不存在");
        accessService.requireView(document.getCaseId());
        Path path = Path.of(document.getFilePath()).toAbsolutePath().normalize();
        if (!path.startsWith(uploadRoot) || !Files.isRegularFile(path)) throw new BusinessException("文件不存在");
        Resource resource = new FileSystemResource(path);
        try {
            String contentType = Files.probeContentType(path);
            return new DownloadedDocument(document, resource, contentType == null ? "application/octet-stream" : contentType);
        } catch (IOException exception) {
            throw new BusinessException("读取文件失败");
        }
    }
}
