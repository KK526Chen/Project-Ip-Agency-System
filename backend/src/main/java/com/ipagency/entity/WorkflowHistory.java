package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("workflow_history")
public class WorkflowHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long instanceId;
    @NotNull private Long caseId;
    @Size(max=80) private String fromState;
    @Size(max=80) private String toState;
    @Size(max=80) private String eventCode;
    private Long operatorUserId;
    private String reason;
    private LocalDateTime occurredAt;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
