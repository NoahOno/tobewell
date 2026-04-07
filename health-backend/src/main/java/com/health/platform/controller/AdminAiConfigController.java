package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.AiProviderConfig;
import com.health.platform.entity.AiServiceConfig;
import com.health.platform.mapper.AiProviderConfigMapper;
import com.health.platform.mapper.AiServiceConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Admin AI Config")
@RestController
@RequestMapping("/admin")
@SaCheckRole("ADMIN")
public class AdminAiConfigController {

    private final AiServiceConfigMapper aiServiceConfigMapper;
    private final AiProviderConfigMapper aiProviderConfigMapper;

    public AdminAiConfigController(AiServiceConfigMapper aiServiceConfigMapper, AiProviderConfigMapper aiProviderConfigMapper) {
        this.aiServiceConfigMapper = aiServiceConfigMapper;
        this.aiProviderConfigMapper = aiProviderConfigMapper;
    }

    @Operation(summary = "List AI services")
    @GetMapping("/ai-services")
    public Result<List<AiServiceConfig>> listServices() {
        List<AiServiceConfig> services = aiServiceConfigMapper.selectList(new LambdaQueryWrapper<AiServiceConfig>()
                .orderByAsc(AiServiceConfig::getSortOrder)
                .orderByDesc(AiServiceConfig::getUpdateTime));
        for (AiServiceConfig service : services) {
            if (service.getApiConfigId() != null) {
                AiProviderConfig provider = aiProviderConfigMapper.selectById(service.getApiConfigId());
                service.setProviderName(provider != null ? provider.getName() : null);
            }
        }
        return Result.success(services);
    }

    @Operation(summary = "Save AI service")
    @PostMapping("/ai-services/save")
    public Result<Void> saveService(@RequestBody AiServiceConfig service) {
        service.setUpdateTime(LocalDateTime.now());
        if (service.getId() == null) {
            service.setCreateTime(LocalDateTime.now());
            if (service.getEnabled() == null) service.setEnabled(true);
            if (service.getSortOrder() == null) service.setSortOrder(0);
            aiServiceConfigMapper.insert(service);
        } else {
            aiServiceConfigMapper.updateById(service);
        }
        return Result.success();
    }

    @Operation(summary = "Toggle AI service")
    @PostMapping("/ai-services/{id}/toggle")
    public Result<Void> toggleService(@PathVariable("id") Integer id) {
        AiServiceConfig service = aiServiceConfigMapper.selectById(id);
        if (service == null) return Result.error("Service not found");
        service.setEnabled(!Boolean.TRUE.equals(service.getEnabled()));
        service.setUpdateTime(LocalDateTime.now());
        aiServiceConfigMapper.updateById(service);
        return Result.success();
    }

    @Operation(summary = "List AI providers")
    @GetMapping("/ai-providers")
    public Result<List<AiProviderConfig>> listProviders() {
        return Result.success(aiProviderConfigMapper.selectList(new LambdaQueryWrapper<AiProviderConfig>()
                .orderByDesc(AiProviderConfig::getIsDefault)
                .orderByDesc(AiProviderConfig::getUpdateTime)));
    }

    @Operation(summary = "Save AI provider")
    @PostMapping("/ai-providers/save")
    public Result<Void> saveProvider(@RequestBody AiProviderConfig provider) {
        provider.setUpdateTime(LocalDateTime.now());
        if (provider.getId() == null) {
            provider.setCreateTime(LocalDateTime.now());
            if (provider.getEnabled() == null) provider.setEnabled(true);
            if (provider.getIsDefault() == null) provider.setIsDefault(false);
            aiProviderConfigMapper.insert(provider);
        } else {
            aiProviderConfigMapper.updateById(provider);
        }
        return Result.success();
    }

    @Operation(summary = "Toggle AI provider")
    @PostMapping("/ai-providers/{id}/toggle")
    public Result<Void> toggleProvider(@PathVariable("id") Integer id) {
        AiProviderConfig provider = aiProviderConfigMapper.selectById(id);
        if (provider == null) return Result.error("Provider not found");
        provider.setEnabled(!Boolean.TRUE.equals(provider.getEnabled()));
        provider.setUpdateTime(LocalDateTime.now());
        aiProviderConfigMapper.updateById(provider);
        return Result.success();
    }

    @Operation(summary = "Set default AI provider")
    @PostMapping("/ai-providers/{id}/default")
    public Result<Void> setDefaultProvider(@PathVariable("id") Integer id) {
        List<AiProviderConfig> providers = aiProviderConfigMapper.selectList(null);
        for (AiProviderConfig provider : providers) {
            provider.setIsDefault(provider.getId().equals(id));
            provider.setUpdateTime(LocalDateTime.now());
            aiProviderConfigMapper.updateById(provider);
        }
        return Result.success();
    }
}
