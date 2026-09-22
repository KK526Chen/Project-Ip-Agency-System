package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("document_series")
public class DocumentSeries {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long caseId;
    @NotBlank @Size(max=40) private String documentType;
    @Size(max=300) private String logicalName;
    private Long currentVersionId;
    private Long approvedVersionId;
    @Size(max=30) private String status;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
