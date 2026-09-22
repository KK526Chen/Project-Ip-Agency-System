package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("agent_skill")
public class AgentSkill {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long agentId;
    @Size(max=40) private String skillType;
    @Size(max=80) private String skillCode;
    @Size(max=40) private String proficiency;
    private LocalDate validFrom;
    private LocalDate validTo;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
