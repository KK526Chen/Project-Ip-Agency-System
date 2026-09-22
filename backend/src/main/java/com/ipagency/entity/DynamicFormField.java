package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("dynamic_form_field")
public class DynamicFormField {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long definitionId;
    @NotBlank @Size(max=80) private String fieldKey;
    @Size(max=200) private String label;
    @Size(max=30) private String fieldType;
    private Integer requiredFlag;
    private String optionsJson;
    private String validation;
    private String visibleCondition;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
