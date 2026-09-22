package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("work_item")
public class WorkItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long caseId;
    private Long stageId;
    private Long deadlineId;
    private Long parentId;
    @NotBlank @Size(max=40) private String workType;
    @NotBlank @Size(max=200) private String title;
    private String description;
    private Long assigneeAgentId;
    private Long creatorUserId;
    @Size(max=20) @Pattern(regexp="TODO|IN_PROGRESS|BLOCKED|REVIEW|DONE|CANCELLED") private String status;
    @Size(max=20) private String priority;
    private LocalDateTime startTime;
    private LocalDateTime dueTime;
    private LocalDateTime completedTime;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    @Size(max=30) private String transferPolicy;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
