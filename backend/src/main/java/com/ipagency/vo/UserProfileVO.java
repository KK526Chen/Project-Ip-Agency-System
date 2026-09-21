package com.ipagency.vo;

import com.ipagency.entity.SysUser;

public record UserProfileVO(Long id, String username, String realName, String role, String phone, String email) {
    public static UserProfileVO from(SysUser user) {
        return new UserProfileVO(user.getId(), user.getUsername(), user.getRealName(), user.getRole(), user.getPhone(), user.getEmail());
    }
}
