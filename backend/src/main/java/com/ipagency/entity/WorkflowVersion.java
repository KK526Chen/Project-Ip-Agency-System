package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("workflow_version")
public class WorkflowVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long definitionId;
    @NotNull private Integer versionNo;
    @Size(max=20) @Pattern(regexp="DRAFT|PUBLISHED|RETIRED") private String status;
    private LocalDateTime publishedAt;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
