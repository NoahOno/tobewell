package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_goal")
public class HealthGoal {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String name;
    private Double targetValue;
    private Double currentValue;
    private String unit;
    private LocalDate deadline;
    private String status;
    private LocalDateTime createTime;
}
