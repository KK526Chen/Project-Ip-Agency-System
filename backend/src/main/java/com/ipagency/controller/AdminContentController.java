package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminContentController {
    private final PublicContentService content;
    public AdminContentController(PublicContentService content) {
        this.content = content;
    }

    @GetMapping("/{resource:service-products|success-cases|announcements}/{id}")
    public ApiResponse<?> detail(@PathVariable String resource, @PathVariable Long id) {
        return ApiResponse.success(content.adminDetail(content.type(resource), id));
    }

    @GetMapping("/{resource:service-products|success-cases|announcements}")
    public ApiResponse<?> list(@PathVariable String resource,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize,
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) String serviceType,
            @RequestParam(required=false) String announcementType) {
        return ApiResponse.success(content.list(content.type(resource),true,pageNum,pageSize,keyword,announcementType == null ? serviceType : announcementType));
    }

    @PostMapping("/{resource:service-products|success-cases|announcements}")
    public ApiResponse<?> create(@PathVariable String resource,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(content.save(content.type(resource),null,body));
    }

    @PutMapping("/{resource:service-products|success-cases|announcements}/{id}")
    public ApiResponse<?> update(@PathVariable String resource,
            @PathVariable Long id,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(content.save(content.type(resource),id,body));
    }

    @DeleteMapping("/{resource:service-products|success-cases|announcements}/{id}")
    public ApiResponse<?> delete(@PathVariable String resource,
            @PathVariable Long id) {
        content.delete(content.type(resource),id);
        return ApiResponse.success(null);
    }
}
