package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.service.DocumentVersionService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class DocumentVersionController {
    private final DocumentVersionService versions;
    public DocumentVersionController(DocumentVersionService versions) { this.versions = versions; }

    @GetMapping("/documents/{id}/versions")
    public ApiResponse<?> versions(@PathVariable Long id) { return ApiResponse.success(versions.versions(id)); }

    @PostMapping(value = "/documents/{id}/versions", consumes = "multipart/form-data")
    public ApiResponse<?> newVersion(@PathVariable Long id, @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String remark) {
        return ApiResponse.success(versions.createVersion(id, file, remark));
    }

    @PostMapping("/documents/{id}/change-requests")
    public ApiResponse<?> change(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(versions.requestChange(id, body));
    }
}
