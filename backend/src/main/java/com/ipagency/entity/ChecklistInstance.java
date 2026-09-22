package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("checklist_instance")
public class ChecklistInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long caseId;
    @NotNull private Long templateId;
    @Size(max=80) private String gateCode;
    @Size(max=20) private String status;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
