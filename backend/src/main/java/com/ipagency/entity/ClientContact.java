package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("client_contact")
public class ClientContact {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long clientId;
    @NotBlank
    @Size(max = 50)
    private String name;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String position;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 20)
    private String phone;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String email;
    @Size(max = 30)
    @Pattern(regexp = "ALL_CASES|SPECIFIED_CASES|FEE_ONLY")
    private String permissionScope;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 500)
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
