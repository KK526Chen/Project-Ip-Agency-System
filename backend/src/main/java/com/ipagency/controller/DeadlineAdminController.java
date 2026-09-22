package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.service.DeadlineEngineService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DeadlineAdminController {
    private final DeadlineEngineService engine;
    public DeadlineAdminController(DeadlineEngineService engine) { this.engine = engine; }

    @GetMapping("/deadlines/{id}/history")
    public ApiResponse<?> history(@PathVariable Long id) { return ApiResponse.success(engine.history(id)); }

    @PostMapping("/deadlines/{id}/adjust")
    public ApiResponse<?> adjust(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(engine.adjust(id, body));
    }

    @GetMapping("/admin/deadline-rules")
    public ApiResponse<?> rules(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(engine.rules(pageNum, pageSize));
    }

    @PostMapping("/admin/deadline-rules")
    public ApiResponse<?> saveRule(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(engine.saveRule(null, body));
    }

    @PutMapping("/admin/deadline-rules/{id}")
    public ApiResponse<?> updateRule(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(engine.saveRule(id, body));
    }

    @GetMapping("/admin/business-calendar")
    public ApiResponse<?> calendars(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(engine.calendars(pageNum, pageSize));
    }

    @GetMapping("/admin/business-calendar/{id}/days")
    public ApiResponse<?> days(@PathVariable Long id, @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        return ApiResponse.success(engine.days(id, from, to));
    }

    @PostMapping("/admin/business-calendar/{id}/days")
    public ApiResponse<?> day(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(engine.upsertDay(id, body));
    }
}
