package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("training_plan")
public class TrainingPlan {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String category;
    private String content;
    private String duration; // e.g. "4 Weeks"
    private String actions;  // JSON String: [{"name":"Squat", "sets":"3x10"}]
    private Boolean isPublic;
    private Integer sourceId;
    private Boolean isSubscribed;
    private String coverImage;
    private String audience;
}
