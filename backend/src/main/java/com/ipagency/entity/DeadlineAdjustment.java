package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("deadline_adjustment")
public class DeadlineAdjustment {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long deadlineId;
    private LocalDateTime oldValue;
    private LocalDateTime newValue;
    private Long operatorUserId;
    private String reason;
    private String attachment;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
