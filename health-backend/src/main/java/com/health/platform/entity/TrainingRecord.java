package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("training_record")
public class TrainingRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String sourceType; // 'SCHEDULE' or 'COURSE'
    private Integer sourceId;
    private Integer completeDuration;
    private String difficulty; // TOO_EASY, GOOD, TOO_HARD
    private String feeling;
    private LocalDateTime recordTime;
}
