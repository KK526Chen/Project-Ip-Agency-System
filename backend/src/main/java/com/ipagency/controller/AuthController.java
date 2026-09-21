package com.ipagency.controller;

import com.ipagency.common.ApiResponse;
import com.ipagency.dto.LoginRequest;
import com.ipagency.dto.PasswordChangeRequest;
import com.ipagency.service.AuthService;
import com.ipagency.vo.LoginVO;
import com.ipagency.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> profile() { return ApiResponse.success(authService.profile()); }

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(request);
        return ApiResponse.success("密码修改成功", null);
    }
}
