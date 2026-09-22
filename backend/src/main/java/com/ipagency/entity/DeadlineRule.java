package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("deadline_rule")
public class DeadlineRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank @Size(max=80) private String ruleCode;
    @Size(max=40) private String businessType;
    @Size(max=80) private String triggerEvent;
    private Integer baseDays;
    @Size(max=20) private String dayType;
    @Size(max=40) private String adjustmentPolicy;
    private Integer internalOffsetDays;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer versionNo;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
