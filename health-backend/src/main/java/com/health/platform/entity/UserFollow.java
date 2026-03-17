package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_follow")
public class UserFollow {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer followerId;
    private Integer followeeId;
    private LocalDateTime createTime;
}
