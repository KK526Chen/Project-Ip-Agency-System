package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.service.OcrPipelineService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OcrController {
    private final OcrPipelineService ocr;
    public OcrController(OcrPipelineService ocr) { this.ocr = ocr; }

    @GetMapping("/ocr-jobs")
    public ApiResponse<?> ocrJobs(@RequestParam(required = false) Long caseId,
            @RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(ocr.list(caseId, pageNum, pageSize));
    }

    @PostMapping("/ocr-jobs/{id}/retry")
    public ApiResponse<?> retryOcr(@PathVariable Long id) { return ApiResponse.success(ocr.retry(id)); }

    @PostMapping("/ocr-jobs/{id}/confirm")
    public ApiResponse<?> confirmOcr(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(ocr.confirm(id, body == null ? Map.of() : body));
    }
}
