package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("work_dependency")
public class WorkDependency {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long caseId;
    @NotNull private Long predecessorId;
    @NotNull private Long successorId;
    @Size(max=30) private String dependencyType;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
