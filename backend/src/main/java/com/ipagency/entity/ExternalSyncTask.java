package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("external_sync_task")
public class ExternalSyncTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long externalSystemId;
    @NotBlank
    @Size(max = 40)
    private String businessType;
    @NotNull
    private Long businessId;
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "PUSH|PULL|STATUS_QUERY")
    private String syncType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String requestPayload;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String responsePayload;
    @Size(max = 20)
    @Pattern(regexp = "PENDING|PROCESSING|SUCCESS|FAILED")
    private String status;
    @Min(0)
    private Integer retryCount;
    @Min(0)
    private Integer maxRetryCount;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime nextRetryTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 2000)
    private String lastError;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime startTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime finishTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
