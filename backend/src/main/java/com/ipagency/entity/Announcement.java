package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank
    @Size(max = 50)
    private String announcementNo;
    @NotBlank
    @Size(max = 200)
    private String title;
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "POLICY|BUSINESS|PROMOTION|RECRUITMENT|SYSTEM|TRAINING")
    private String announcementType;
    @Size(max = 50)
    private String targetScope;
    @NotBlank
    private String content;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime publishTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate startDate;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate endDate;
    @Min(0)
    @Max(1)
    private Integer isTop;
    @Min(0)
    @Max(1)
    private Integer status;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long publisherUserId;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0)
    @Max(1)
    private Integer isDeleted;
}
