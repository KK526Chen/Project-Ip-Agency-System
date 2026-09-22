package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("timesheet")
public class Timesheet {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long workItemId;
    @NotNull private Long caseId;
    @NotNull private Long agentId;
    @NotNull private LocalDate workDate;
    @NotNull @DecimalMin("0.01") private BigDecimal hours;
    private String description;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
