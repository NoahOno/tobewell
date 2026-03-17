package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community_post")
public class CommunityPost {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private String category;
    private String tags;
    private Integer likeCount;
    private Integer collectionCount;
    private Integer commentCount;
    private Integer viewCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String nickname;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Boolean isLiked;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Boolean isCollected;
}
