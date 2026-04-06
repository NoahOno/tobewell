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

    // 1: General Check-in, 2: Content Challenge, 3: Topic
    private Integer activityType;

    // 'PLAN' or 'COURSE' (Used for type 2)
    private String templateType;
    // training_plan.id or course.id
    private Integer templateId;

    // Used for type 3
    private String topicName;

    // Type 1 check-in & type 2 challenge: COUNT (by times) or DAYS (by days / distinct days)
    private String countMode;

    // Topic activity stat mode: SHARED (binary), DAYS (share days), COUNT (share times)
    private String topicStatMode;

    // Rewards
    private Integer rewardPoints;

    private Integer requiredDays;

    // 1 pinned, 0 normal
    private Integer pinned;

    // DRAFT/ONLINE/OFFLINE
    private String status;

    private LocalDateTime createTime;
}

