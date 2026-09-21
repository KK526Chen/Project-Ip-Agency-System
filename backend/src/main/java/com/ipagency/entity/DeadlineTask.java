package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("deadline_task")
public class DeadlineTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long caseId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long agentId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long documentId;
    @NotBlank
    @Size(max = 40)
    private String deadlineType;
    @NotBlank
    @Size(max = 200)
    private String taskName;
    @NotNull
    private LocalDateTime officialDeadline;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime internalDeadline;
    @Size(max = 20)
    @Pattern(regexp = "NOT_STARTED|IN_PROGRESS|COMPLETED|OVERDUE")
    private String status;
    @Size(max = 20)
    @Pattern(regexp = "URGENT|HIGH|MEDIUM|LOW")
    private String priority;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 50)
    private String remindType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime completedTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 1000)
    private String description;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
