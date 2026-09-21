package com.ipagency.service;

import com.ipagency.entity.CaseDocument;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {
    CaseDocument upload(MultipartFile file, Long caseId, String documentType, String remark);
    DownloadedDocument download(Long documentId);
    record DownloadedDocument(CaseDocument document, Resource resource, String contentType) { }
}
