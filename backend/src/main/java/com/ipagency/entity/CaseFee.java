package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("case_fee")
public class CaseFee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long caseId;
    private String feeType;
    private BigDecimal amount;
    private String direction;
    private String status;
    private LocalDate payDate;
    private Long operatorId;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
