package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.service.ChecklistService;
import com.ipagency.service.WorkflowService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WorkflowController {
    private final WorkflowService workflows;
    private final ChecklistService checklists;
    public WorkflowController(WorkflowService workflows, ChecklistService checklists) {
        this.workflows = workflows; this.checklists = checklists;
    }

    @GetMapping("/admin/workflows")
    public ApiResponse<?> defs(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(workflows.definitions(pageNum, pageSize));
    }

    @PostMapping("/admin/workflows")
    public ApiResponse<?> createWf(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(workflows.createDefinition(body));
    }

    @PostMapping("/admin/workflows/{id}/versions")
    public ApiResponse<?> wfVersion(@PathVariable Long id) { return ApiResponse.success(workflows.newVersion(id)); }

    @PostMapping("/admin/workflow-versions/{id}/publish")
    public ApiResponse<?> publish(@PathVariable Long id) { return ApiResponse.success(workflows.publish(id)); }

    @GetMapping("/cases/{id}/workflow")
    public ApiResponse<?> caseWf(@PathVariable Long id) { return ApiResponse.success(workflows.detail(id)); }

    @PostMapping("/cases/{id}/workflow/events/{event}")
    public ApiResponse<?> fire(@PathVariable Long id, @PathVariable String event, @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(workflows.fire(id, event, body == null ? null : (String) body.get("reason")));
    }

    @GetMapping("/cases/{id}/checklists")
    public ApiResponse<?> checklists(@PathVariable Long id) {
        var inst = checklists.list(id);
        return ApiResponse.success(inst.stream().map(i -> Map.of("instance", i, "items", checklists.items(i.getId()))).toList());
    }

    @PutMapping("/checklist-items/{id}")
    public ApiResponse<?> checkItem(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean checked = Boolean.TRUE.equals(body.get("checked")) || Integer.valueOf(1).equals(body.get("checkedFlag"));
        return ApiResponse.success(checklists.check(id, checked));
    }
}
