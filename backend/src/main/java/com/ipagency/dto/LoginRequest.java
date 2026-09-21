package com.ipagency.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "用户名不能为空") @jakarta.validation.constraints.Size(max = 50) String username,
        @NotBlank(message = "密码不能为空") @jakarta.validation.constraints.Size(max = 72) String password) {
    @Override public String toString() { return "LoginRequest[username=" + username + ", password=<redacted>]"; }
}
