package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_schedule")
public class DailySchedule {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String sourceType; // 'PLAN' or 'COURSE'
    private Integer planId;
    private Integer courseId;
    private LocalDate date;
    private String title;
    private String description;
    private String actions; // JSON String
    private String status; // PENDING, COMPLETED, SKIPPED
    private LocalDateTime createTime;
}
