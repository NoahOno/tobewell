package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_metric")
public class HealthMetric {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String name;
    private Double value;
    private String unit;
    private LocalDateTime recordTime;
}
