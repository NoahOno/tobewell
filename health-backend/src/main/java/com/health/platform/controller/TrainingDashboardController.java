package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.Course;
import com.health.platform.entity.DailySchedule;
import com.health.platform.entity.TrainingRecord;
import com.health.platform.mapper.CourseMapper;
import com.health.platform.mapper.DailyScheduleMapper;
import com.health.platform.mapper.TrainingRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Training Dashboard")
@RestController
@RequestMapping("/training/dashboard")
@SaCheckLogin
public class TrainingDashboardController {

    @Autowired
    private TrainingRecordMapper trainingRecordMapper;

    @Autowired
    private DailyScheduleMapper dailyScheduleMapper;

    @Autowired
    private CourseMapper courseMapper;

    private static final List<String> TRAINING_SOURCE_TYPES = List.of("COURSE", "SCHEDULE");

    private List<TrainingRecord> getTrainingRecords(Integer userId) {
        return trainingRecordMapper.selectList(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .in(TrainingRecord::getSourceType, TRAINING_SOURCE_TYPES));
    }

    @Operation(summary = "Get training dashboard summary")
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary() {
        Integer userId = StpUtil.getLoginIdAsInt();
        List<TrainingRecord> records = getTrainingRecords(userId);

        int totalDurationMinutes = records.stream()
                .map(TrainingRecord::getCompleteDuration)
                .filter(v -> v != null)
                .mapToInt(Integer::intValue)
                .sum();

        int completedCount = records.size();

        // Continuous training days: compute a current streak ending at the latest completed day.
        Map<LocalDate, Long> dateCountMap = records.stream()
                .filter(r -> r.getRecordTime() != null)
                .collect(Collectors.groupingBy(r -> r.getRecordTime().toLocalDate(), Collectors.counting()));

        int currentStreakDays = 0;
        if (!dateCountMap.isEmpty()) {
            LocalDate latest = dateCountMap.keySet().stream().max(Comparator.naturalOrder()).orElse(LocalDate.now());
            LocalDate cursor = latest;
            while (dateCountMap.containsKey(cursor)) {
                currentStreakDays++;
                cursor = cursor.minusDays(1);
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("totalDurationMinutes", totalDurationMinutes);
        resp.put("currentStreakDays", currentStreakDays);
        resp.put("completedCount", completedCount);
        return Result.success(resp);
    }

    @Operation(summary = "Get last N days training trend")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(@RequestParam(value = "days", defaultValue = "7") int days) {
        Integer userId = StpUtil.getLoginIdAsInt();
        LocalDate today = LocalDate.now();
        int d = Math.max(1, Math.min(30, days));

        LocalDate start = today.minusDays(d - 1L);
        LocalDateTime startDt = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endDt = LocalDateTime.of(today, LocalTime.MAX);

        List<TrainingRecord> records = trainingRecordMapper.selectList(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .in(TrainingRecord::getSourceType, TRAINING_SOURCE_TYPES)
                .between(TrainingRecord::getRecordTime, startDt, endDt));

        Map<LocalDate, List<TrainingRecord>> byDate = records.stream()
                .filter(r -> r.getRecordTime() != null)
                .collect(Collectors.groupingBy(r -> r.getRecordTime().toLocalDate()));

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM-dd");

        List<String> labels = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (int i = 0; i < d; i++) {
            LocalDate day = start.plusDays(i);
            labels.add(day.format(labelFmt));

            List<TrainingRecord> list = byDate.get(day);
            if (list == null || list.isEmpty()) {
                durations.add(0);
                counts.add(0);
            } else {
                int dur = list.stream()
                        .map(TrainingRecord::getCompleteDuration)
                        .filter(v -> v != null)
                        .mapToInt(Integer::intValue)
                        .sum();
                durations.add(dur);
                counts.add(list.size());
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("labels", labels);
        resp.put("durations", durations);
        resp.put("counts", counts);
        return Result.success(resp);
    }

    @Operation(summary = "Get latest training items")
    @GetMapping("/recent")
    public Result<Map<String, Object>> getRecent(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        Integer userId = StpUtil.getLoginIdAsInt();
        int l = Math.max(1, Math.min(50, limit));

        List<TrainingRecord> records = trainingRecordMapper.selectList(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .in(TrainingRecord::getSourceType, TRAINING_SOURCE_TYPES)
                .orderByDesc(TrainingRecord::getRecordTime)
                .last("LIMIT " + l));

        List<Map<String, Object>> items = new ArrayList<>();
        for (TrainingRecord r : records) {
            LocalDate date = r.getRecordTime() != null ? r.getRecordTime().toLocalDate() : null;
            String dateStr = date != null ? date.toString() : "";
            int duration = r.getCompleteDuration() != null ? r.getCompleteDuration() : 0;

            String title = "训练";
            if ("COURSE".equals(r.getSourceType())) {
                Course c = courseMapper.selectById(r.getSourceId());
                if (c != null && c.getTitle() != null) title = c.getTitle();
            } else if ("SCHEDULE".equals(r.getSourceType())) {
                DailySchedule s = dailyScheduleMapper.selectById(r.getSourceId());
                if (s != null && s.getTitle() != null) title = s.getTitle();
            }

            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("title", title);
            item.put("durationMinutes", duration);
            items.add(item);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        return Result.success(resp);
    }
}

