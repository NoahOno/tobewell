package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.WorkoutRecord;
import com.health.platform.mapper.WorkoutMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Workout Records")
@RestController
@RequestMapping("/workout")
@SaCheckLogin
public class WorkoutController {

    @Autowired
    private WorkoutMapper workoutMapper;

    @Operation(summary = "Record a workout session")
    @PostMapping("/record")
    public Result<Void> record(@RequestBody WorkoutRecord record) {
        record.setUserId(StpUtil.getLoginIdAsInt());
        if (record.getRecordTime() == null) {
            record.setRecordTime(LocalDateTime.now());
        }
        workoutMapper.insert(record);
        return Result.success();
    }

    @Operation(summary = "Get workout statistics")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        List<WorkoutRecord> allRecords = workoutMapper.selectList(new LambdaQueryWrapper<WorkoutRecord>()
                .eq(WorkoutRecord::getUserId, userId));

        int totalDuration = allRecords.stream().mapToInt(WorkoutRecord::getDuration).sum();
        
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        int todayDuration = allRecords.stream()
                .filter(r -> r.getRecordTime().isAfter(startOfToday))
                .mapToInt(WorkoutRecord::getDuration)
                .sum();

        Map<String, Integer> categoryStats = allRecords.stream()
                .collect(Collectors.groupingBy(WorkoutRecord::getType, Collectors.summingInt(WorkoutRecord::getDuration)));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDuration", totalDuration);
        stats.put("todayDuration", todayDuration);
        stats.put("categoryStats", categoryStats);
        
        return Result.success(stats);
    }
}
