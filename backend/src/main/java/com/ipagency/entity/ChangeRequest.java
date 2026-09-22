package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("change_request")
public class ChangeRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long seriesId;
    @NotNull private Long sourceVersionId;
    private Long newDocumentId;
    @NotNull private Long requesterUserId;
    private Long approverUserId;
    @Size(max=20) @Pattern(regexp="PENDING|APPROVED|REJECTED|COMPLETED") private String status;
    private String reason;
    private String impact;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
