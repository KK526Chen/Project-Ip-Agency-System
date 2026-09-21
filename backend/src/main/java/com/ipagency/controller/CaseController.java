package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.CaseInfo;
import com.ipagency.entity.CaseMember;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
public class CaseController {
    @GetMapping public ApiResponse<PageResult<CaseInfo>> list(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) { return ApiResponse.success(PageResult.empty(pageNum, pageSize)); }
    @GetMapping("/{id}") public ApiResponse<CaseInfo> detail(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
    @PostMapping public ApiResponse<Void> create(@RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @PutMapping("/{id}") public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
    @GetMapping("/{caseId}/members") public ApiResponse<List<CaseMember>> members(@PathVariable Long caseId) { return SkeletonSupport.notImplemented(); }
    @PostMapping("/{caseId}/members") public ApiResponse<Void> addMember(@PathVariable Long caseId, @RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @DeleteMapping("/{caseId}/members/{id}") public ApiResponse<Void> removeMember(@PathVariable Long caseId, @PathVariable Long id) { return SkeletonSupport.notImplemented(); }
}
