package com.ipagency.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ipagency.entity.ClientInfo;
import com.ipagency.mapper.ClientInfoMapper;
import com.ipagency.service.ClientInfoService;
import org.springframework.stereotype.Service;
@Service public class ClientInfoServiceImpl extends ServiceImpl<ClientInfoMapper, ClientInfo> implements ClientInfoService { }
