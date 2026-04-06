package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.HealthContent;
import com.health.platform.mapper.ContentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Content Management")
@RestController
@RequestMapping("/content")
@SaCheckLogin
public class ContentController {

    @Autowired
    private ContentMapper contentMapper;

    @Operation(summary = "Get all content (Admin only)")
    @SaCheckRole("ADMIN")
    @GetMapping("/all")
    public Result<List<HealthContent>> getAll() {
        return Result.success(contentMapper.selectList(null));
    }

    @Operation(summary = "Get user's own content")
    @GetMapping("/my")
    public Result<List<HealthContent>> getMyContent() {
        Integer userId = StpUtil.getLoginIdAsInt();
        return Result.success(contentMapper.selectList(new LambdaQueryWrapper<HealthContent>()
                .eq(HealthContent::getAuthorId, userId)));
    }

    @Operation(summary = "Create content")
    @PostMapping("/create")
    public Result<Void> create(@RequestBody HealthContent content) {
        content.setAuthorId(StpUtil.getLoginIdAsInt());
        content.setCreateTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.insert(content);
        return Result.success();
    }

    @Operation(summary = "Update content")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody HealthContent content) {
        HealthContent old = contentMapper.selectById(content.getId());
        if (old == null) return Result.error("Content not found");
        
        // Check permission: owner or admin
        if (!old.getAuthorId().equals(StpUtil.getLoginIdAsInt()) && !StpUtil.hasRole("ADMIN")) {
            return Result.error("Permission denied");
        }

        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
        return Result.success();
    }

    @Operation(summary = "Delete content")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        HealthContent old = contentMapper.selectById(id);
        if (old == null) return Result.success();

        // Check permission: owner or admin
        if (!old.getAuthorId().equals(StpUtil.getLoginIdAsInt()) && !StpUtil.hasRole("ADMIN")) {
            return Result.error("Permission denied");
        }

        contentMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "Get trending topics")
    @GetMapping("/trending")
    public Result<List<java.util.Map<String, Object>>> getTrending() {
        return Result.success(contentMapper.selectMaps(new LambdaQueryWrapper<HealthContent>()
                .select(HealthContent::getCategory)
                .groupBy(HealthContent::getCategory)
                .last("LIMIT 5"))
                .stream()
                .map(m -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("name", m.get("category"));
                    map.put("count", (int)(Math.random() * 20) + 1);
                    return map;
                })
                .collect(java.util.stream.Collectors.toList()));
    }
}
