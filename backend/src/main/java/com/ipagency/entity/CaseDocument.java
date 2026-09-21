package com.ipagency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("case_document")
public class CaseDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long caseId;
    private String documentName;
    private String documentType;
    private String filePath;
    private Long uploaderId;
    private String remark;
    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
