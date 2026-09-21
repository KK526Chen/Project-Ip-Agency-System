package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("invoice_record")
public class InvoiceRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long billId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String invoiceNo;
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "NORMAL|SPECIAL")
    private String invoiceType;
    @NotBlank
    @Size(max = 200)
    private String invoiceTitle;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String taxpayerNo;
    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime issueTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @JsonIgnore
    @Size(max = 1000)
    private String filePath;
    @Size(max = 20)
    @Pattern(regexp = "PENDING|ISSUED|VOID")
    private String status;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
