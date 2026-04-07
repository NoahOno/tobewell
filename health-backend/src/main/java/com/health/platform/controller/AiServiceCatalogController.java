package com.health.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.AiServiceConfig;
import com.health.platform.mapper.AiServiceConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AI Service Catalog")
@RestController
@RequestMapping("/ai")
public class AiServiceCatalogController {

    private final AiServiceConfigMapper aiServiceConfigMapper;

    public AiServiceCatalogController(AiServiceConfigMapper aiServiceConfigMapper) {
        this.aiServiceConfigMapper = aiServiceConfigMapper;
    }

    @Operation(summary = "List enabled AI services")
    @GetMapping("/services")
    public Result<List<AiServiceConfig>> listEnabledServices() {
        return Result.success(aiServiceConfigMapper.selectList(new LambdaQueryWrapper<AiServiceConfig>()
                .eq(AiServiceConfig::getEnabled, true)
                .orderByAsc(AiServiceConfig::getSortOrder)
                .orderByAsc(AiServiceConfig::getId)));
    }
}
