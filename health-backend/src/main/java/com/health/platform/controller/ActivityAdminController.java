package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.Activity;
import com.health.platform.entity.ActivityParticipation;
import com.health.platform.entity.ActivityTask;
import com.health.platform.entity.ActivityDynamic;
import com.health.platform.entity.DailySchedule;
import com.health.platform.mapper.*;
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

@Tag(name = "Administrator Activities")
@RestController
@RequestMapping("/admin/activities")
@SaCheckRole("ADMIN")
public class ActivityAdminController {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityParticipationMapper participationMapper;

    @Autowired
    private ActivityTaskMapper taskMapper;

    @Autowired
    private ActivityDynamicMapper dynamicMapper;

    @Operation(summary = "Get activities (admin)")
    @GetMapping
    public Result<List<Activity>> list() {
        List<Activity> list = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .orderByDesc(Activity::getPinned)
                .orderByAsc(Activity::getStartTime));
        return Result.success(list);
    }

    @Operation(summary = "Create or update an activity")
    @PostMapping
    public Result<Void> upsert(@RequestBody Activity activity) {
        if (activity == null || activity.getTitle() == null) {
            return Result.error("Invalid activity");
        }
        if (activity.getStatus() == null) activity.setStatus("ONLINE");
        if (activity.getPinned() == null) activity.setPinned(0);
        if (activity.getRequiredDays() == null) activity.setRequiredDays(7);

        if (activity.getId() == null) {
            activityMapper.insert(activity);
        } else {
            activityMapper.updateById(activity);
        }
        return Result.success();
    }

    @Operation(summary = "Offline an activity")
    @PostMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Integer id) {
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");
        act.setStatus("OFFLINE");
        activityMapper.updateById(act);
        return Result.success();
    }

    public static class PinReq {
        private Integer pinned;

        public Integer getPinned() { return pinned; }
        public void setPinned(Integer pinned) { this.pinned = pinned; }
    }

    @Operation(summary = "Pin or unpin an activity")
    @PostMapping("/{id}/pin")
    public Result<Void> pin(@PathVariable Integer id, @RequestBody PinReq req) {
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");
        int p = req != null && req.getPinned() != null ? req.getPinned() : 0;
        act.setPinned(p);
        activityMapper.updateById(act);
        return Result.success();
    }

    @Operation(summary = "Delete an activity")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        // Delete in dependency order: dynamics -> tasks -> participation -> activity
        List<ActivityParticipation> parts = participationMapper.selectList(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, id));
        List<Integer> partIds = parts.stream().map(ActivityParticipation::getId).collect(Collectors.toList());

        if (!partIds.isEmpty()) {
            taskMapper.delete(new LambdaQueryWrapper<ActivityTask>().in(ActivityTask::getParticipationId, partIds));
        }
        dynamicMapper.delete(new LambdaQueryWrapper<ActivityDynamic>().eq(ActivityDynamic::getActivityId, id));
        participationMapper.delete(new LambdaQueryWrapper<ActivityParticipation>().eq(ActivityParticipation::getActivityId, id));
        activityMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "Get activity analytics (admin)")
    @GetMapping("/{id}/analytics")
    public Result<Map<String, Object>> analytics(@PathVariable Integer id) {
        List<ActivityParticipation> allParts = participationMapper.selectList(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, id));
        long totalParticipants = allParts.size();
        long completedParticipants = allParts.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();

        // dailyActive: distinct users who completed any activity_task today (口径 A)
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);

        List<ActivityTask> todaysCompletedTasks = taskMapper.selectList(new LambdaQueryWrapper<ActivityTask>()
                .eq(ActivityTask::getStatus, "COMPLETED")
                .ge(ActivityTask::getCompletedTime, start)
                .le(ActivityTask::getCompletedTime, end));

        List<Integer> todaysPartIds = todaysCompletedTasks.stream()
                .map(ActivityTask::getParticipationId)
                .distinct()
                .collect(Collectors.toList());

        List<ActivityParticipation> todaysParts = todaysPartIds.isEmpty()
                ? List.of()
                : participationMapper.selectList(new LambdaQueryWrapper<ActivityParticipation>()
                .in(ActivityParticipation::getId, todaysPartIds));

        long dailyActive = todaysParts.stream().map(ActivityParticipation::getUserId).distinct().count();

        double completionRate = totalParticipants == 0 ? 0.0 : (double) completedParticipants / (double) totalParticipants;

        Map<String, Object> resp = new HashMap<>();
        resp.put("totalParticipants", totalParticipants);
        resp.put("dailyActive", dailyActive);
        resp.put("completedParticipants", completedParticipants);
        resp.put("completionRate", completionRate);
        return Result.success(resp);
    }
}

