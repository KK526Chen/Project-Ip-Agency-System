package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CaseController {
    private final CaseWorkflowService cases;
    private final V2Store db;
    public CaseController(CaseWorkflowService cases, V2Store db) {
        this.cases = cases;
        this.db = db;
    }

    @GetMapping({"/cases", "/client/cases", "/agent/cases", "/admin/cases"})
    public ApiResponse<?> list(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String caseType,
            @RequestParam(required=false) String keyword) {
        return ApiResponse.success(cases.list(pageNum,pageSize,status,caseType,keyword));
    }

    @GetMapping("/admin/case-reviews")
    public ApiResponse<?> reviews(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(cases.list(pageNum,pageSize,"PENDING_REVIEW",null,null));
    }

    @GetMapping({"/cases/{id}", "/client/cases/{id}", "/agent/cases/{id}", "/admin/cases/{id}"})
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.success(cases.detail(id));
    }

    @PostMapping("/client/cases")
    public ApiResponse<?> create(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(cases.save(null,body));
    }

    @PutMapping("/client/cases/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ApiResponse.success(cases.save(id,body));
    }

    @PostMapping("/client/cases/{id}/submit")
    public ApiResponse<?> submit(@PathVariable Long id) {
        return ApiResponse.success(cases.submit(id));
    }

    @PostMapping("/admin/cases/{id}/review")
    public ApiResponse<?> review(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ApiResponse.success(cases.review(id,body));
    }

    @PostMapping("/admin/cases/{id}/assign")
    public ApiResponse<?> assign(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ApiResponse.success(cases.assign(id,Input.id(body,"agentId"),(String)body.get("reason"),false));
    }

    @PostMapping("/admin/cases/{id}/reassign")
    public ApiResponse<?> reassign(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ApiResponse.success(cases.assign(id,Input.id(body,"agentId"),(String)body.get("reason"),true));
    }

    @PostMapping("/cases/{id}/collaborators")
    public ApiResponse<?> addCollaborator(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ApiResponse.success(cases.addCollaborator(id, Input.id(body, "agentId"), (String) body.get("reason")));
    }

    @DeleteMapping("/cases/{id}/collaborators/{assignmentId}")
    public ApiResponse<?> removeCollaborator(@PathVariable Long id, @PathVariable Long assignmentId) {
        return ApiResponse.success(cases.removeCollaborator(id, assignmentId));
    }

    @GetMapping("/cases/{id}/parties")
    public ApiResponse<?> parties(@PathVariable Long id,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(cases.children(CaseParty.class,id,pageNum,pageSize,"id"));
    }

    @GetMapping("/cases/{id}/priorities")
    public ApiResponse<?> priorities(@PathVariable Long id,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(cases.children(CasePriority.class,id,pageNum,pageSize,"priority_date"));
    }

    @GetMapping("/cases/{id}/assignments")
    public ApiResponse<?> assignments(@PathVariable Long id,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(cases.children(CaseAssignment.class,id,pageNum,pageSize,"assign_time"));
    }

    @GetMapping("/cases/{id}/stages")
    public ApiResponse<?> stages(@PathVariable Long id,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(cases.children(CaseStage.class,id,pageNum,pageSize,"start_time"));
    }

    @PostMapping("/cases/{id}/stages")
    public ApiResponse<?> stage(@PathVariable Long id,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(cases.stage(id,null,body));
    }

    @PutMapping("/case-stages/{id}")
    public ApiResponse<?> stageUpdate(@PathVariable Long id,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(cases.stage(db.get(CaseStage.class,id).getCaseId(),id,body));
    }

    @GetMapping("/cases/{id}/reviews")
    public ApiResponse<?> history(@PathVariable Long id,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(cases.children(ReviewRecord.class,id,pageNum,pageSize,"review_time"));
    }
}
