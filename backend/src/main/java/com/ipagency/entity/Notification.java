package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long userId;
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "DEADLINE|OFFICIAL_DOCUMENT|CASE_STATUS|MATERIAL_REVIEW|PAYMENT|SYSTEM")
    private String notificationType;
    @NotBlank
    @Size(max = 200)
    private String title;
    @NotBlank
    private String content;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 40)
    private String businessType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long businessId;
    @Min(0)
    @Max(1)
    private Integer isRead;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime readTime;
    @Size(max = 30)
    @Pattern(regexp = "SYSTEM|WEB_PUSH|EMAIL|SMS|APP_PUSH")
    private String sendChannel;
    @Size(max = 20)
    @Pattern(regexp = "PENDING|SUCCESS|FAILED")
    private String sendStatus;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
