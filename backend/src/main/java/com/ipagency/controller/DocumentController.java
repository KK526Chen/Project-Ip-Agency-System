package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DocumentController {
    private final DocumentService documents; private final DocumentStorageService storage;
    public DocumentController(DocumentService documents, DocumentStorageService storage) {
        this.documents = documents;
        this.storage = storage;
    }

    @GetMapping("/documents")
    public ApiResponse<?> list(@RequestParam(required=false) Long caseId,
            @RequestParam(required=false) String documentType,
            @RequestParam(required=false) String reviewStatus,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(documents.list(caseId,documentType,reviewStatus,pageNum,pageSize));
    }

    @GetMapping("/cases/{id}/documents")
    public ApiResponse<?> byCase(@PathVariable Long id,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(documents.list(id,null,null,pageNum,pageSize));
    }

    @PostMapping(value="/documents/upload", consumes="multipart/form-data")
    public ApiResponse<?> upload(@RequestPart("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam Long caseId,
            @RequestParam String documentType,
            @RequestParam(required=false) Long stageId,
            @RequestParam(required=false) String remark) {
        return ApiResponse.success(storage.upload(file,caseId,documentType,stageId,remark));
    }

    @GetMapping("/documents/{id}/download")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long id) {
        var result = storage.download(id);
        return org.springframework.http.ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
            .header("X-Content-Type-Options", "nosniff")
            .header("Content-Disposition", org.springframework.http.ContentDisposition.attachment().filename(result.document().getDocumentName(),java.nio.charset.StandardCharsets.UTF_8).build().toString()).body(result.resource());
    }

    @PostMapping("/documents/{id}/ocr")
    public ApiResponse<?> ocr(@PathVariable Long id,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(documents.ocr(id,body));
    }

    @PostMapping("/reviews")
    public ApiResponse<?> review(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(documents.review(body));
    }
}
