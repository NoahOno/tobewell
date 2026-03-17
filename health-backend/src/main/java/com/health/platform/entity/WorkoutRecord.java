package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("workout_record")
public class WorkoutRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String type; // e.g., Running, Cycling, Swimming
    private Integer duration; // in minutes
    private LocalDateTime recordTime;
}
