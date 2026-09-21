package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("client_info")
public class ClientInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String clientName;
    private String clientType;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String remark;
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
