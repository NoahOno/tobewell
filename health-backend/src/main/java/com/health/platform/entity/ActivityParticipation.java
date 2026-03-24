package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_participation")
public class ActivityParticipation {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer activityId;
    private Integer userId;

    // APPLIED/COMPLETED/CANCELLED
    private String status;

    private LocalDateTime applyTime;
    private LocalDateTime completedTime;
}

