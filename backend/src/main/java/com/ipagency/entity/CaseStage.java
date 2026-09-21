package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("case_stage")
public class CaseStage {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long caseId;
    @NotBlank
    @Size(max = 40)
    @Pattern(regexp = "SUBMISSION|ACCEPTANCE|PRELIMINARY_EXAM|PUBLICATION|SUBSTANTIVE_EXAM|OFFICE_ACTION|CLIENT_RESPONSE|GRANT|CERTIFICATE|OTHER")
    private String stageType;
    @NotBlank
    @Size(max = 100)
    private String stageName;
    @NotNull
    private LocalDateTime startTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime endTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long handlerUserId;
    @Size(max = 20)
    @Pattern(regexp = "NOT_STARTED|IN_PROGRESS|COMPLETED")
    private String status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
