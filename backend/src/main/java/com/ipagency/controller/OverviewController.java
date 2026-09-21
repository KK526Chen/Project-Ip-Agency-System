package com.ipagency.controller;

import com.ipagency.common.ApiResponse;
import com.ipagency.common.PageResult;
import com.ipagency.service.OverviewService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/overview")
public class OverviewController {
    private final OverviewService overviewService;
    public OverviewController(OverviewService overviewService) { this.overviewService = overviewService; }
    @GetMapping public ApiResponse<Map<String, Long>> overview() { return ApiResponse.success(overviewService.overview()); }
    @GetMapping("/my-tasks") public ApiResponse<PageResult<Object>> myTasks(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) { return ApiResponse.success(overviewService.myTasks(pageNum, pageSize)); }
    @GetMapping("/deadlines") public ApiResponse<PageResult<Object>> deadlines(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) { return ApiResponse.success(overviewService.deadlines(pageNum, pageSize)); }
}
