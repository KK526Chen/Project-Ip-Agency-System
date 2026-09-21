package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.CaseProgress;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/progress")
public class ProgressController {
    @GetMapping public ApiResponse<PageResult<CaseProgress>> list(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize, @RequestParam(required = false) Long caseId) { return ApiResponse.success(PageResult.empty(pageNum, pageSize)); }
    @PostMapping public ApiResponse<Void> create(@RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
}
