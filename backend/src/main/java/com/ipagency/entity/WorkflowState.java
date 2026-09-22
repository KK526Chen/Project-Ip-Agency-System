package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("workflow_state")
public class WorkflowState {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long versionId;
    @NotBlank @Size(max=80) private String stateCode;
    @Size(max=200) private String stateName;
    private Integer isInitial;
    private Integer isFinal;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
