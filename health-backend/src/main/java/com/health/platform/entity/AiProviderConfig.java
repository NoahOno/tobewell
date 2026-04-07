package com.health.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_provider_config")
public class AiProviderConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String providerType;
    private String baseUrl;
    private String apiKey;
    private String model;
    private String extraHeadersJson;
    private Boolean enabled;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
