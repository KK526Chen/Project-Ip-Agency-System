package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("case_info")
public class CaseInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String caseNo;
    private String caseName;
    private Long clientId;
    private String caseType;
    private String businessType;
    private String applicationNo;
    private Long principalId;
    private String status;
    private String priority;
    private LocalDate startDate;
    private LocalDate closeDate;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
