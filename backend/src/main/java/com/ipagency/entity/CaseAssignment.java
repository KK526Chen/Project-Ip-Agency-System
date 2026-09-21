package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("case_assignment")
public class CaseAssignment {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long caseId;
    @NotNull
    private Long agentId;
    @NotNull
    private Long assignedByUserId;
    @Size(max = 30)
    @Pattern(regexp = "PRINCIPAL|COLLABORATOR")
    private String assignmentRole;
    private LocalDateTime assignTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime endTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 500)
    private String reason;
    @Min(0)
    @Max(1)
    private Integer isCurrent;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
