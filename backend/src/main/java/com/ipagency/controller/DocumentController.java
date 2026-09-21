package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.CaseDocument;
import com.ipagency.service.DocumentStorageService;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentStorageService storageService;
    public DocumentController(DocumentStorageService storageService) { this.storageService = storageService; }

    @GetMapping public ApiResponse<PageResult<CaseDocument>> list(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize, @RequestParam(required = false) Long caseId) { return ApiResponse.success(PageResult.empty(pageNum, pageSize)); }
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CaseDocument> upload(@RequestPart("file") MultipartFile file, @RequestParam Long caseId, @RequestParam String documentType, @RequestParam(required = false) String remark) {
        return ApiResponse.success("上传成功", storageService.upload(file, caseId, documentType, remark));
    }
    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long id) {
        var result = storageService.download(id);
        ContentDisposition disposition = ContentDisposition.attachment().filename(result.document().getDocumentName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(result.resource());
    }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
}
