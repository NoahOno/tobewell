package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String description;
    private String category;
    private String difficulty; // 初级, 中级, 高级
    private Integer durationMinutes;
    private String actionsJson;
    private Boolean isPublic;
    private Integer creatorId;
    private String coverImage;
    private String audience;
    private LocalDateTime createTime;
}
