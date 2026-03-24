package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_dynamic")
public class ActivityDynamic {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer activityId;
    private Integer participationId;
    private Integer userId;

    private String content;

    private LocalDateTime createTime;
}

