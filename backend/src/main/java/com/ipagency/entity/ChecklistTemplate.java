package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("checklist_template")
public class ChecklistTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank @Size(max=80) private String code;
    @Size(max=200) private String name;
    @Size(max=80) private String gateCode;
    private String itemsJson;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
