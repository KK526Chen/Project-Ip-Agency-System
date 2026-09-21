package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.CaseFee;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/fees")
public class FeeController {
    @GetMapping public ApiResponse<PageResult<CaseFee>> list(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize, @RequestParam(required = false) Long caseId) { return ApiResponse.success(PageResult.empty(pageNum, pageSize)); }
    @PostMapping public ApiResponse<Void> create(@RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @PutMapping("/{id}") public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
}
