package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DeadlineController {
    private final DeadlineService deadlines;
    public DeadlineController(DeadlineService deadlines) {
        this.deadlines = deadlines;
    }

    @GetMapping("/deadlines")
    public ApiResponse<?> list(@RequestParam(required=false) Long caseId,
            @RequestParam(required=false) String status,
            @RequestParam(defaultValue="false") boolean upcoming,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(deadlines.list(caseId,status,upcoming,pageNum,pageSize));
    }

    @GetMapping("/cases/{id}/deadlines")
    public ApiResponse<?> byCase(@PathVariable Long id,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(deadlines.list(id,null,false,pageNum,pageSize));
    }

    @PostMapping("/deadlines")
    public ApiResponse<?> create(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(deadlines.save(null,body));
    }

    @PutMapping("/deadlines/{id}")
    public ApiResponse<?> update(@PathVariable Long id,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(deadlines.save(id,body));
    }

    @PostMapping("/deadlines/{id}/complete")
    public ApiResponse<?> complete(@PathVariable Long id) {
        return ApiResponse.success(deadlines.complete(id));
    }
}
