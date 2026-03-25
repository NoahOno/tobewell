package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_submission")
public class ResourceSubmission {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer submitterId;
    private String resourceType; // PLAN / COURSE
    private Integer resourceId;
    private String status; // PENDING / APPROVED / REJECTED
    private String note;
    private Integer reviewerId;
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;
}

