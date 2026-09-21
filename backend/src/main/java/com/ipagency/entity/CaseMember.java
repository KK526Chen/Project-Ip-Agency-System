package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("case_member")
public class CaseMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long caseId;
    private Long userId;
    private String memberRole;
    private LocalDateTime joinTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
