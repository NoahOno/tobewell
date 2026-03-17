package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_star")
public class HealthStar {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer targetId;
    private String targetType; // 'CONTENT' or 'PLAN'
    private LocalDateTime createTime;
}
