package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("exception_case")
public class ExceptionCase {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long caseId;
    @Size(max=60) private String exceptionType;
    private String title;
    private String detail;
    @Size(max=20) @Pattern(regexp="OPEN|ASSIGNED|PROCESSING|RESOLVED|CLOSED") private String status;
    private Long relatedId;
    private Long assigneeUserId;
    private String resolutionNote;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
