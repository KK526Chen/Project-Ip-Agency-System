package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external-sync")
public class ExternalSyncController {
    private final ExternalSyncService sync;
    public ExternalSyncController(ExternalSyncService sync) {
        this.sync = sync;
    }

    @GetMapping("/systems")
    public ApiResponse<?> systems(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(sync.systems(pageNum,pageSize));
    }

    @GetMapping("/tasks")
    public ApiResponse<?> tasks(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize,
            @RequestParam(required=false) String status) {
        return ApiResponse.success(sync.list(pageNum,pageSize,status));
    }

    @PostMapping("/tasks")
    public ApiResponse<?> create(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(sync.create(body));
    }

    @PostMapping("/tasks/{id}/execute")
    public ApiResponse<?> execute(@PathVariable Long id) {
        return ApiResponse.success(sync.execute(id,false));
    }

    @PostMapping("/tasks/{id}/retry")
    public ApiResponse<?> retry(@PathVariable Long id) {
        return ApiResponse.success(sync.execute(id,true));
    }
}
