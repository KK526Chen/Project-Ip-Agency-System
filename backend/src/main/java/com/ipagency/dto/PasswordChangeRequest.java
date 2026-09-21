package com.ipagency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank(message = "原密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") @Size(min = 6, max = 72, message = "新密码长度应为 6 到 72 位") String newPassword) {
}
