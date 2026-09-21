package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("client_profile")
public class ClientProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long userId;
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "INDIVIDUAL|COMPANY|UNIVERSITY|RESEARCH_INSTITUTE")
    private String clientType;
    @NotBlank
    @Size(max = 200)
    private String clientName;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String creditOrIdNo;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 500)
    private String registeredAddress;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 500)
    private String contactAddress;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 50)
    private String primaryContactName;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 20)
    private String primaryContactPhone;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String primaryContactEmail;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String industry;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 500)
    private String technicalPreference;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String invoiceTitle;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String taxpayerNo;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String bankName;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String bankAccount;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
