package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_service_config")
public class AiServiceConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String serviceKey;
    private String name;
    private String description;
    private String tagLabel;
    private String styleLabel;
    private String systemPrompt;
    private Integer sortOrder;
    private Boolean enabled;
    private Integer apiConfigId;
    private String defaultIntent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String providerName;
}
