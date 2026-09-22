package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WorkItemController {
    private final WorkItemService workItems;
    public WorkItemController(WorkItemService workItems) { this.workItems = workItems; }

    @GetMapping("/work-items")
    public ApiResponse<?> list(@RequestParam(required=false) Long caseId, @RequestParam(required=false) String status,
            @RequestParam(defaultValue="1") long pageNum, @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(workItems.list(caseId, status, pageNum, pageSize));
    }
    @GetMapping("/cases/{id}/work-items")
    public ApiResponse<?> byCase(@PathVariable Long id, @RequestParam(defaultValue="1") long pageNum, @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(workItems.list(id, null, pageNum, pageSize));
    }
    @PostMapping("/work-items")
    public ApiResponse<?> create(@RequestBody Map<String,Object> body) { return ApiResponse.success(workItems.save(null, body)); }
    @PutMapping("/work-items/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Map<String,Object> body) { return ApiResponse.success(workItems.save(id, body)); }
    @PostMapping("/work-items/{id}/start")
    public ApiResponse<?> start(@PathVariable Long id, @RequestBody(required=false) Map<String,Object> body) {
        boolean override = body != null && Boolean.TRUE.equals(body.get("override"));
        return ApiResponse.success(workItems.start(id, override, body == null ? null : (String) body.get("reason")));
    }
    @PostMapping("/work-items/{id}/complete")
    public ApiResponse<?> complete(@PathVariable Long id) { return ApiResponse.success(workItems.complete(id)); }
    @PostMapping("/work-items/{id}/dependencies")
    public ApiResponse<?> dep(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ApiResponse.success(workItems.addDependency(id, Input.id(body, "predecessorId"), (String) body.get("dependencyType")));
    }
    @GetMapping("/timesheets")
    public ApiResponse<?> timesheets(@RequestParam(required=false) Long workItemId,
            @RequestParam(defaultValue="1") long pageNum, @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(workItems.timesheets(workItemId, pageNum, pageSize));
    }
    @PostMapping("/timesheets")
    public ApiResponse<?> addTime(@RequestBody Map<String,Object> body) { return ApiResponse.success(workItems.addTimesheet(body)); }
    @PutMapping("/timesheets/{id}")
    public ApiResponse<?> ignoredUpdate(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ApiResponse.success(workItems.addTimesheet(body));
    }
    @DeleteMapping("/timesheets/{id}")
    public ApiResponse<?> deleteTime(@PathVariable Long id) { workItems.deleteTimesheet(id); return ApiResponse.success(true); }
}
