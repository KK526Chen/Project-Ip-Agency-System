package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("agent_profile")
public class AgentProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long userId;
    @NotBlank
    @Size(max = 50)
    private String employeeNo;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String licenseNo;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 30)
    @Pattern(regexp = "PATENT|TRADEMARK|COPYRIGHT|LITIGATION|PROCESS")
    private String department;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String professionalField;
    @Min(0)
    private Integer practiceYears;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String education;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 500)
    private String ipcScope;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String profile;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 500)
    private String signaturePath;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
