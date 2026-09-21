package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank
    @Size(max = 50)
    private String username;
    @JsonIgnore
    @lombok.ToString.Exclude
    @NotBlank
    @Size(max = 100)
    private String passwordHash;
    @NotBlank
    @Size(max = 50)
    private String realName;
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "CLIENT|AGENT|ADMIN")
    private String role;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 20)
    private String phone;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String email;
    @Min(0)
    @Max(1)
    private Integer status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime lastLoginTime;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
