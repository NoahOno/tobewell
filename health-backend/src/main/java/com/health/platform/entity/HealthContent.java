package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_content")
public class HealthContent {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private Integer authorId;
    private String category;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
