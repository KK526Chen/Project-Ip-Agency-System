package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("service_product")
public class ServiceProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank
    @Size(max = 50)
    private String serviceNo;
    @NotBlank
    @Size(max = 200)
    private String serviceName;
    @NotBlank
    @Size(max = 40)
    @Pattern(regexp = "PATENT_APPLICATION|TRADEMARK_REGISTRATION|COPYRIGHT_REGISTRATION|IP_STANDARD|IP_PROTECTION|PATENT_ANALYSIS")
    private String serviceType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String targetType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String processDesc;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal officialFee;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal agencyFee;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String estimatedCycle;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String requiredMaterials;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String advantages;
    @Min(0)
    @Max(1)
    private Integer status;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
