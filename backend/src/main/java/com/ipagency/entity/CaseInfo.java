package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("case_info")
public class CaseInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 60)
    private String caseNo;
    @NotBlank
    @Size(max = 300)
    private String caseName;
    @NotNull
    private Long clientId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long serviceProductId;
    @NotBlank
    @Size(max = 40)
    @Pattern(regexp = "INVENTION_PATENT|UTILITY_MODEL|DESIGN_PATENT|TRADEMARK|COPYRIGHT|INVALIDATION|INFRINGEMENT_LITIGATION|OTHER")
    private String caseType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 300)
    private String technicalField;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String applicationNo;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long principalAgentId;
    @Size(max = 40)
    @Pattern(regexp = "SUBMITTED|PENDING_REVIEW|RETURNED|PENDING_ASSIGNMENT|PROCESSING|FORMAL_EXAM|SUBSTANTIVE_EXAM|PRELIMINARY_PASSED|GRANTED|REJECTED|REEXAMINATION|WITHDRAWN|EXPIRED|CLOSED")
    private String status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String currentStage;
    @Size(max = 20)
    @Pattern(regexp = "URGENT|HIGH|MEDIUM|LOW")
    private String priorityLevel;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime submitTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime acceptTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate expectedNextOfficialDate;
    @Min(0)
    @Max(1)
    private Integer confidentialReview;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
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
