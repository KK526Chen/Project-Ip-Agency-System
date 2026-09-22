package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("ocr_extracted_field")
public class OcrExtractedField {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long jobId;
    @NotBlank @Size(max=80) private String fieldName;
    private String rawValue;
    private String parsedValue;
    private BigDecimal confidence;
    private String confirmedValue;
    private Long confirmedBy;
    private LocalDateTime confirmedAt;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
