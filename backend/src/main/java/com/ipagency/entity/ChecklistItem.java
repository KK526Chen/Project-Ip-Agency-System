package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("checklist_item")
public class ChecklistItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long instanceId;
    @NotBlank @Size(max=200) private String itemLabel;
    private Integer requiredFlag;
    private Integer checkedFlag;
    private String missingMessage;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
