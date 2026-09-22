package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.service.GovernanceService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GovernanceController {
    private final GovernanceService governance;
    public GovernanceController(GovernanceService governance) { this.governance = governance; }

    @GetMapping("/risks")
    public ApiResponse<?> risks(@RequestParam(required = false) Long caseId,
            @RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(governance.risks(caseId, pageNum, pageSize));
    }

    @GetMapping("/cases/{id}/risks")
    public ApiResponse<?> caseRisks(@PathVariable Long id) { return ApiResponse.success(governance.risks(id, 1, 100)); }

    @GetMapping("/admin/exceptions")
    public ApiResponse<?> exceptions(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(governance.exceptions(status, pageNum, pageSize));
    }

    @PutMapping("/admin/exceptions/{id}/assign")
    public ApiResponse<?> assignEx(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(governance.assign(id, Input.id(body, "userId")));
    }

    @PostMapping("/admin/exceptions/{id}/resolve")
    public ApiResponse<?> resolveEx(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(governance.resolve(id, (String) body.get("resolutionNote")));
    }

    @GetMapping("/admin/data-quality")
    public ApiResponse<?> dq() { return ApiResponse.success(governance.scan()); }

    @GetMapping("/admin/agent-recommendations")
    public ApiResponse<?> recs(@RequestParam Long caseId) { return ApiResponse.success(governance.recommend(caseId)); }
}
