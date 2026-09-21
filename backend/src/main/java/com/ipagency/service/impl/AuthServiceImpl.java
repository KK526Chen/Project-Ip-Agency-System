package com.ipagency.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ipagency.common.BusinessException;
import com.ipagency.common.CurrentUserContext;
import com.ipagency.common.JwtUtil;
import com.ipagency.dto.LoginRequest;
import com.ipagency.dto.PasswordChangeRequest;
import com.ipagency.entity.SysUser;
import com.ipagency.mapper.SysUserMapper;
import com.ipagency.service.AuthService;
import com.ipagency.vo.LoginVO;
import com.ipagency.vo.UserProfileVO;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final com.ipagency.service.BusinessEvents events;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(SysUserMapper userMapper, JwtUtil jwtUtil, com.ipagency.service.BusinessEvents events) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.events = events;
    }

    @Override
    @Transactional
    public LoginVO login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.username()));
        if (user == null || user.getStatus() == null || user.getStatus() != 1
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误", HttpStatus.UNAUTHORIZED);
        }
        if (!java.util.Set.of("CLIENT", "AGENT", "ADMIN").contains(user.getRole())) throw new BusinessException("用户角色无效", HttpStatus.UNAUTHORIZED);
        user.setLastLoginTime(java.time.LocalDateTime.now());
        int updated = userMapper.update(null, Wrappers.<SysUser>lambdaUpdate()
                .eq(SysUser::getId, user.getId()).eq(SysUser::getStatus, 1)
                .set(SysUser::getLastLoginTime, user.getLastLoginTime()));
        if (updated == 0) throw new BusinessException("用户已停用", HttpStatus.UNAUTHORIZED);
        events.auditAs(user.getId(), user.getRole(), "LOGIN", "USER", user.getId());
        return new LoginVO(jwtUtil.generate(user.getId(), user.getUsername(), user.getRole()), UserProfileVO.from(user));
    }

    @Override
    public UserProfileVO profile() {
        SysUser user = userMapper.selectById(CurrentUserContext.require().userId());
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已停用", HttpStatus.UNAUTHORIZED);
        }
        return UserProfileVO.from(user);
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getId, CurrentUserContext.require().userId()).last("FOR UPDATE"));
        if (user == null || !passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
        events.audit("CHANGE_PASSWORD", "USER", user.getId());
    }
}
