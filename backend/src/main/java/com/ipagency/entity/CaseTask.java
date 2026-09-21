package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("case_task")
public class CaseTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long caseId;
    private String title;
    private String description;
    private Long assigneeId;
    private Long creatorId;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
