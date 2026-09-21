package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("fee_bill")
public class FeeBill {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank
    @Size(max = 60)
    private String billNo;
    @NotNull
    private Long caseId;
    @NotNull
    private Long clientId;
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "OFFICIAL|AGENCY|EXPEDITE|OTHER")
    private String feeType;
    @NotBlank
    @Size(max = 200)
    private String feeItem;
    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal discountAmount;
    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal payableAmount;
    @Size(max = 30)
    @Pattern(regexp = "PENDING_CONFIRM|PENDING_PAYMENT|PAID|INVOICED|REFUNDED|CANCELLED")
    private String status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate dueDate;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 1000)
    private String remark;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long createdByUserId;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
