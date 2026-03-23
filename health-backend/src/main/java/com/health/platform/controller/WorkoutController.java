package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.WorkoutRecord;
import com.health.platform.entity.TrainingRecord;
import com.health.platform.mapper.WorkoutMapper;
import com.health.platform.mapper.TrainingRecordMapper;
import com.health.platform.mapper.DailyScheduleMapper;
import com.health.platform.mapper.TrainingMapper;
import com.health.platform.mapper.CourseMapper;
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
    @Autowired
    private TrainingRecordMapper trainingRecordMapper;
    @Autowired
    private DailyScheduleMapper scheduleMapper;
    @Autowired
    private TrainingMapper trainingMapper;
    @Autowired
    private CourseMapper courseMapper;

    @Operation(summary = "Record a manual workout session")
    @PostMapping("/record")
    public Result<Void> record(@RequestBody WorkoutRecord record) {
        record.setUserId(StpUtil.getLoginIdAsInt());
        if (record.getRecordTime() == null) {
            record.setRecordTime(LocalDateTime.now());
        }
        workoutMapper.insert(record);
        return Result.success();
    }

    @Operation(summary = "Get combined workout and training statistics")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        // 1. Manual Workout Records
        List<WorkoutRecord> manualRecords = workoutMapper.selectList(new LambdaQueryWrapper<WorkoutRecord>()
                .eq(WorkoutRecord::getUserId, userId));

        // 2. Training Session Records (Completed Schedules/Courses)
        List<TrainingRecord> systemRecords = trainingRecordMapper.selectList(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId));

        int totalDuration = manualRecords.stream().mapToInt(WorkoutRecord::getDuration).sum();
        totalDuration += systemRecords.stream().mapToInt(TrainingRecord::getCompleteDuration).sum();
        
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        int todayDuration = manualRecords.stream()
                .filter(r -> r.getRecordTime().isAfter(startOfToday))
                .mapToInt(WorkoutRecord::getDuration)
                .sum();
        todayDuration += systemRecords.stream()
                .filter(r -> r.getRecordTime().isAfter(startOfToday))
                .mapToInt(TrainingRecord::getCompleteDuration)
                .sum();

        Map<String, Integer> categoryStats = new HashMap<>();
        
        // Add manual records stats
        manualRecords.forEach(r -> {
            categoryStats.merge(r.getType(), r.getDuration(), Integer::sum);
        });
        
        // Add system records stats (requires looking up categories)
        systemRecords.forEach(r -> {
            String category = "系统训练";
            if ("COURSE".equals(r.getSourceType())) {
                com.health.platform.entity.Course course = courseMapper.selectById(r.getSourceId());
                if (course != null && course.getCategory() != null) category = course.getCategory();
            } else if ("SCHEDULE".equals(r.getSourceType())) {
                com.health.platform.entity.DailySchedule schedule = scheduleMapper.selectById(r.getSourceId());
                if (schedule != null) {
                    if (schedule.getPlanId() != null) {
                        com.health.platform.entity.TrainingPlan plan = trainingMapper.selectById(schedule.getPlanId());
                        if (plan != null && plan.getCategory() != null) category = plan.getCategory();
                    } else if (schedule.getCourseId() != null) {
                        com.health.platform.entity.Course course = courseMapper.selectById(schedule.getCourseId());
                        if (course != null && course.getCategory() != null) category = course.getCategory();
                    }
                }
            }
            categoryStats.merge(category, r.getCompleteDuration(), Integer::sum);
        });

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDuration", totalDuration);
        stats.put("todayDuration", todayDuration);
        stats.put("categoryStats", categoryStats);
        
        return Result.success(stats);
    }
}
