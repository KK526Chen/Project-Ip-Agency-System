package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
public class ClientController {
    private final ProfileService profiles;
    public ClientController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping("/profile")
    public ApiResponse<ClientProfile> profile() {
        return ApiResponse.success(profiles.client());
    }

    @PutMapping("/profile")
    public ApiResponse<ClientProfile> profile(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(profiles.saveClient(body));
    }

    @GetMapping("/contacts")
    public ApiResponse<?> contacts(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(profiles.contacts(pageNum,pageSize));
    }

    @PostMapping("/contacts")
    public ApiResponse<?> create(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(profiles.contact(null,body));
    }

    @PutMapping("/contacts/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ApiResponse.success(profiles.contact(id,body));
    }

    @DeleteMapping("/contacts/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        profiles.deleteContact(id);
        return ApiResponse.success(null);
    }
}
