package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("external_case_binding")
public class ExternalCaseBinding {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long caseId;
    @NotNull
    private Long externalSystemId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String externalCaseId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String externalApplicationNo;
    @Size(max = 20)
    @Pattern(regexp = "PENDING|SUCCESS|FAILED")
    private String syncStatus;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime lastSyncTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 1000)
    private String lastError;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
