package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {
    private final UserManagementService users;
    public UserController(UserManagementService users) {
        this.users = users;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize,
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) String role) {
        return ApiResponse.success(users.list(pageNum,pageSize,keyword,role));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(users.save(null,body));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(users.save(id,body));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> status(@PathVariable Long id,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(users.save(id,body));
    }
}
