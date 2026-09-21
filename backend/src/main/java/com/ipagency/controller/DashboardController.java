package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboard; private final ProfileService profiles;
    public DashboardController(DashboardService dashboard, ProfileService profiles) {
        this.dashboard = dashboard;
        this.profiles = profiles;
    }

    @GetMapping({"/client/dashboard", "/agent/dashboard", "/admin/dashboard", "/overview"})
    public ApiResponse<?> overview() {
        return ApiResponse.success(dashboard.overview());
    }

    @GetMapping({"/agent/performance", "/admin/statistics"})
    public ApiResponse<?> statistics() {
        return ApiResponse.success(dashboard.statistics());
    }

    @GetMapping("/agent/profile")
    public ApiResponse<?> profile() {
        return ApiResponse.success(profiles.agent());
    }

    @PutMapping("/agent/profile")
    public ApiResponse<?> profile(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(profiles.saveAgent(body));
    }
}
