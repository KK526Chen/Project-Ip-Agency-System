package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("risk_record")
public class RiskRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long caseId;
    @Size(max=40) private String riskType;
    @Size(max=20) private String levelCode;
    private String title;
    private String description;
    @Size(max=20) private String status;
    private Long relatedId;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
