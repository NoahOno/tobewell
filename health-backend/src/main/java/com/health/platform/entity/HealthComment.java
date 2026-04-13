package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("health_comment")
public class HealthComment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer targetId;
    private String targetType; // 'CONTENT' or 'PLAN'
    private Integer parentId;
    private String content;
    private LocalDateTime createTime;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String nickname;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String avatar;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<HealthComment> replies = new ArrayList<>();
}
