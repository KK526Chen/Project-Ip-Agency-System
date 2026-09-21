package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("user_device")
public class UserDevice {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long userId;
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "WEB|ANDROID|IOS")
    private String deviceType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 1000)
    private String deviceToken;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 30)
    private String pushPlatform;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String deviceName;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime lastActiveTime;
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
