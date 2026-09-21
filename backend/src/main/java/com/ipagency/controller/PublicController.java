package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final PublicContentService content;
    public PublicController(PublicContentService content) {
        this.content = content;
    }

    @GetMapping("/{resource:services|success-cases|announcements}")
    public ApiResponse<?> list(@PathVariable String resource,
            @RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize,
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) String serviceType,
            @RequestParam(required=false) String announcementType) {
        return ApiResponse.success(content.list(content.type(resource),false,pageNum,pageSize,keyword,announcementType == null ? serviceType : announcementType));
    }

    @GetMapping("/{resource:services|success-cases|announcements}/{id}")
    public ApiResponse<?> detail(@PathVariable String resource,
            @PathVariable Long id) {
        return ApiResponse.success(content.detail(content.type(resource),id));
    }
}
