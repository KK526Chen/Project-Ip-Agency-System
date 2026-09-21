package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("success_case")
public class SuccessCase {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank
    @Size(max = 50)
    private String caseNo;
    @NotBlank
    @Size(max = 200)
    private String caseName;
    @NotBlank
    @Size(max = 40)
    private String serviceType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String clientIndustry;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String technicalField;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String highlights;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String result;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate grantDate;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;
    @Min(0)
    @Max(1)
    private Integer publishStatus;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
