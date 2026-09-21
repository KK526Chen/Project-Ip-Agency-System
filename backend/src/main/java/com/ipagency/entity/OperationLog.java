package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long userId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 50)
    private String username;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 20)
    private String role;
    @NotBlank
    @Size(max = 50)
    private String module;
    @NotBlank
    @Size(max = 100)
    private String operation;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 50)
    private String businessType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long businessId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 20)
    private String requestMethod;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 1000)
    private String requestPath;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 64)
    private String ipAddress;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 30)
    private String deviceType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 1000)
    private String userAgent;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String detailJson;
    @Size(max = 20)
    @Pattern(regexp = "SUCCESS|FAILED")
    private String result;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 2000)
    private String errorMessage;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
