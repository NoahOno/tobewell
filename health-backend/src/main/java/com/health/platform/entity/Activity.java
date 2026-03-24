package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;
    private String coverImage;
    private String descriptionHtml;

    // Frontend posts "yyyy-MM-dd HH:mm:ss" (space separator).
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    // 'PLAN' or 'COURSE'
    private String templateType;
    // training_plan.id or course.id
    private Integer templateId;

    private Integer requiredDays;

    // 1 pinned, 0 normal
    private Integer pinned;

    // ONLINE/OFFLINE
    private String status;

    private LocalDateTime createTime;
}

