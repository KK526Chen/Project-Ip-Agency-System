package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("workflow_transition")
public class WorkflowTransition {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long versionId;
    @NotBlank @Size(max=80) private String fromState;
    @NotBlank @Size(max=80) private String eventCode;
    @NotBlank @Size(max=80) private String toState;
    @Size(max=40) private String allowedRole;
    @Size(max=80) private String guardRule;
    private String actions;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
