package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("review_record")
public class ReviewRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long caseId;
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "CASE|DOCUMENT|SUPPLEMENT|OTHER")
    private String targetType;
    @NotNull
    private Long targetId;
    @NotNull
    private Long reviewerUserId;
    @NotBlank
    @Size(max = 30)
    private String reviewType;
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "APPROVED|MINOR_REVISION|MAJOR_REVISION|REJECTED|RETURNED|RESUBMIT_REQUIRED")
    private String reviewResult;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String reviewComment;
    private LocalDateTime reviewTime;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
