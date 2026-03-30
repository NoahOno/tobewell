package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exercise")
public class Exercise {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String muscle;
    private String type;
    private String equipment;
    private String difficulty;
    private String instruction;
    private String commonErrors; // JSON string
    private String recommendedSets;
    private String imageUrl;
    private String coverImage;
    private String videoUrl;
    private Boolean isPublic;
    private String duration;
    private LocalDateTime createTime;
}
