package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("case_party")
public class CaseParty {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long caseId;
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "APPLICANT|INVENTOR|DESIGNER|RIGHT_HOLDER")
    private String partyType;
    @NotBlank
    @Size(max = 200)
    private String name;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String idNo;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String nationality;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 500)
    private String address;
    @Min(0)
    @Max(1)
    private Integer isPrimary;
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
