package com.ipagency.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ipagency.entity.SysUser;
import com.ipagency.mapper.SysUserMapper;
import com.ipagency.service.SysUserService;
import org.springframework.stereotype.Service;
@Service public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService { }
