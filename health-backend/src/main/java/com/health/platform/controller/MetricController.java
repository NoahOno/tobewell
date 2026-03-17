package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.HealthMetric;
import com.health.platform.mapper.MetricMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Health Metrics")
@RestController
@RequestMapping("/metric")
@SaCheckLogin
public class MetricController {

    @Autowired
    private MetricMapper metricMapper;

    @Operation(summary = "Get user's metrics by name")
    @GetMapping("/list")
    public Result<List<HealthMetric>> getMetrics(@RequestParam String name) {
        Integer userId = StpUtil.getLoginIdAsInt();
        return Result.success(metricMapper.selectList(new LambdaQueryWrapper<HealthMetric>()
                .eq(HealthMetric::getUserId, userId)
                .eq(HealthMetric::getName, name)
                .orderByAsc(HealthMetric::getRecordTime)));
    }

    @Operation(summary = "Get latest metrics for dashboard")
    @GetMapping("/latest")
    public Result<List<HealthMetric>> getLatestMetrics() {
        Integer userId = StpUtil.getLoginIdAsInt();
        // Get the latest record for each metric name for this user
        // Using a custom SQL or simply fetching more records to group in memory if needed, 
        // but for SQLite/H2 typical in these projects, a simple query works.
        // We'll fetch the last 100 to increase likelihood of hitting all types, 
        // or better, we can use a group by approach if the DB supports it well.
        return Result.success(metricMapper.selectList(new LambdaQueryWrapper<HealthMetric>()
                .eq(HealthMetric::getUserId, userId)
                .orderByDesc(HealthMetric::getRecordTime)
                .last("LIMIT 100"))); 
    }

    @Operation(summary = "Record a metric")
    @PostMapping("/record")
    public Result<Void> record(@RequestBody HealthMetric metric) {
        metric.setUserId(StpUtil.getLoginIdAsInt());
        if (metric.getRecordTime() == null) {
            metric.setRecordTime(LocalDateTime.now());
        }
        metricMapper.insert(metric);
        return Result.success();
    }
}
