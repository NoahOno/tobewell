package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("activity_task")
public class ActivityTask {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer participationId;
    private Integer dailyScheduleId;

    private LocalDate scheduledDate;
    private Integer taskIndex;

    // PENDING/COMPLETED/SKIPPED
    private String status;
    private LocalDateTime completedTime;
}

