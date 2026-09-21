package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("external_system")
public class ExternalSystem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank
    @Size(max = 50)
    private String systemCode;
    @NotBlank
    @Size(max = 200)
    private String systemName;
    @NotBlank
    @Size(max = 50)
    private String systemType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 1000)
    private String baseUrl;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 30)
    private String authType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @JsonIgnore
    @Size(max = 500)
    private String credentialRef;
    @Min(0)
    @Max(1)
    private Integer status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 1000)
    private String remark;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
