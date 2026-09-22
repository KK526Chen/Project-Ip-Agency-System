package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("deadline_reminder_log")
public class DeadlineReminderLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long deadlineId;
    @Size(max=20) private String nodeCode;
    private LocalDate remindDate;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
