package com.ipagency.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ipagency.entity.CaseMember;
import com.ipagency.mapper.CaseMemberMapper;
import com.ipagency.service.CaseMemberService;
import org.springframework.stereotype.Service;
@Service public class CaseMemberServiceImpl extends ServiceImpl<CaseMemberMapper, CaseMember> implements CaseMemberService { }
