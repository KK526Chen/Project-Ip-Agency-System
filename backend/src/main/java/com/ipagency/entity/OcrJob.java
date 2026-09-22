package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("ocr_job")
public class OcrJob {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long documentId;
    @NotNull private Long caseId;
    @Size(max=50) private String engine;
    @Size(max=50) private String engineVersion;
    @Size(max=20) @Pattern(regexp="PENDING|PROCESSING|EXTRACTED|NEEDS_REVIEW|CONFIRMED|FAILED") private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String rawText;
    private String errorMessage;
    private Integer retryCount;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
