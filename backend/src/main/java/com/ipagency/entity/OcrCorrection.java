package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("ocr_correction")
public class OcrCorrection {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long fieldId;
    private String oldValue;
    private String newValue;
    private Long operatorUserId;
    private String reason;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
