package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("subscription")
public class Subscription {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer planId;
    private String status;
    private LocalDateTime createTime;
}
