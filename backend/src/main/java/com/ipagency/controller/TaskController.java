package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.CaseTask;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/tasks")
public class TaskController {
    @GetMapping public ApiResponse<PageResult<CaseTask>> list(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize, @RequestParam(required = false) Long caseId) { return ApiResponse.success(PageResult.empty(pageNum, pageSize)); }
    @GetMapping("/{id}") public ApiResponse<CaseTask> detail(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
    @PostMapping public ApiResponse<Void> create(@RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @PutMapping("/{id}") public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
}
