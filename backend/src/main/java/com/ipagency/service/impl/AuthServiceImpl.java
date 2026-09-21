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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(SysUserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.username()));
        if (user == null || user.getStatus() == null || user.getStatus() != 1
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误", HttpStatus.UNAUTHORIZED);
        }
        return new LoginVO(jwtUtil.generate(user.getId(), user.getRole()), UserProfileVO.from(user));
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
        SysUser user = userMapper.selectById(CurrentUserContext.require().userId());
        if (user == null || !passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }
}
