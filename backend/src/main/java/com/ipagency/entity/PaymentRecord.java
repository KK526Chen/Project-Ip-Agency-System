package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("payment_record")
public class PaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long billId;
    @NotBlank
    @Size(max = 80)
    private String paymentNo;
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "BANK|ALIPAY|WECHAT|SIMULATED")
    private String paymentMethod;
    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime paymentTime;
    @Size(max = 20)
    @Pattern(regexp = "PENDING|SUCCESS|FAILED|REFUNDED")
    private String status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String externalTransactionNo;
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
