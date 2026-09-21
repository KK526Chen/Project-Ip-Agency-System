package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("case_document")
public class CaseDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long caseId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long stageId;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String documentNo;
    @NotBlank
    @Size(max = 300)
    private String documentName;
    @NotBlank
    @Size(max = 40)
    @Pattern(regexp = "TECHNICAL_DISCLOSURE|TRADEMARK_IMAGE|APPLICATION|OFFICE_ACTION_RESPONSE|OFFICIAL|SUPPLEMENT|INTERNAL|CERTIFICATE|OTHER")
    private String documentType;
    @Size(max = 30)
    @Pattern(regexp = "CLIENT|AGENT|ADMIN|OFFICIAL|EXTERNAL_SYSTEM")
    private String sourceType;
    @JsonIgnore
    @NotBlank
    @Size(max = 1000)
    private String filePath;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 300)
    private String originalFileName;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long fileSize;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 100)
    private String mimeType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long uploaderUserId;
    @Size(max = 30)
    @Pattern(regexp = "NOT_REQUIRED|PENDING|APPROVED|MINOR_REVISION|MAJOR_REVISION|REJECTED|RESUBMIT_REQUIRED")
    private String reviewStatus;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate officialIssueDate;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime receiveTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate officialDeadline;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal feeAmountExtracted;
    @Size(max = 20)
    @Pattern(regexp = "NOT_STARTED|PROCESSING|SUCCESS|FAILED|MANUAL")
    private String ocrStatus;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String ocrText;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String ocrExtractedJson;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 50)
    private String externalSystemCode;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Size(max = 200)
    private String externalDocumentId;
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
