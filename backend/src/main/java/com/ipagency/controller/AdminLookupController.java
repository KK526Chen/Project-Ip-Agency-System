package com.ipagency.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.ApiResponse;
import com.ipagency.entity.*;
import com.ipagency.service.V2Store;
import org.springframework.web.bind.annotation.*;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@RestController
@RequestMapping("/api/admin")
public class AdminLookupController {
    private final V2Store db;
    public AdminLookupController(V2Store db) {
        this.db = db;
    }

    @GetMapping("/agents")
    public ApiResponse<?> agents(@RequestParam(defaultValue = "1") long pageNum,
                                 @RequestParam(defaultValue = "10") long pageSize,
                                 @RequestParam(required = false) String keyword) {
        role("ADMIN");
        return ApiResponse.success(db.page(AgentProfile.class, new QueryWrapper<AgentProfile>()
            .inSql("user_id", "SELECT id FROM sys_user WHERE role='AGENT' AND status=1 AND is_deleted=0")
            .like(keyword != null, "employee_no", keyword).orderByAsc("id"), pageNum, pageSize));
    }

    @GetMapping("/clients")
    public ApiResponse<?> clients(@RequestParam(defaultValue = "1") long pageNum,
                                  @RequestParam(defaultValue = "10") long pageSize,
                                  @RequestParam(required = false) String keyword) {
        role("ADMIN");
        return ApiResponse.success(db.page(ClientProfile.class, new QueryWrapper<ClientProfile>()
            .like(keyword != null, "client_name", keyword).orderByAsc("id"), pageNum, pageSize));
    }

    @GetMapping("/operation-logs")
    public ApiResponse<?> logs(@RequestParam(defaultValue = "1") long pageNum,
                               @RequestParam(defaultValue = "10") long pageSize,
                               @RequestParam(required = false) String module) {
        role("ADMIN");
        return ApiResponse.success(db.page(OperationLog.class, new QueryWrapper<OperationLog>()
            .eq(module != null, "module", module).orderByDesc("create_time", "id"), pageNum, pageSize));
    }
}
