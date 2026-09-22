package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("data_quality_issue")
public class DataQualityIssue {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max=80) private String issueCode;
    private Long caseId;
    private String detail;
    @Size(max=20) private String status;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
