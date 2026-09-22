package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("calendar_day")
public class CalendarDay {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull private Long calendarId;
    @NotNull private LocalDate dayDate;
    private Integer isWorkday;
    @Size(max=100) private String holidayName;
    @Size(max=30) private String overrideType;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(update = "NOW()")
    private LocalDateTime updateTime;
    @TableLogic
    @Min(0) @Max(1)
    private Integer isDeleted;
}
