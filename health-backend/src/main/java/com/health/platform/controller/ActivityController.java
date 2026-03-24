package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.*;
import com.health.platform.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Community Activities")
@RestController
@RequestMapping("/activity")
@SaCheckLogin
public class ActivityController {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityParticipationMapper participationMapper;

    @Autowired
    private ActivityTaskMapper taskMapper;

    @Autowired
    private ActivityDynamicMapper dynamicMapper;

    @Autowired
    private DailyScheduleMapper dailyScheduleMapper;

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "Get all online activities")
    @GetMapping("/list")
    public Result<List<Activity>> list() {
        Integer userId = StpUtil.getLoginIdAsInt();
        // userId currently unused, but keeps API signature consistent for future personalized filtering.
        userId = userId;

        List<Activity> list = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, "ONLINE")
                .orderByDesc(Activity::getPinned)
                .orderByAsc(Activity::getStartTime));
        return Result.success(list);
    }

    @Operation(summary = "Get trending activities (Top 3)")
    @GetMapping("/trending")
    public Result<List<Activity>> trending() {
        List<Activity> list = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, "ONLINE")
                .orderByDesc(Activity::getPinned)
                .orderByAsc(Activity::getStartTime)
                .last("LIMIT 3"));
        return Result.success(list);
    }

    @Operation(summary = "Get activity detail")
    @GetMapping("/{id}")
    public Result<Activity> detail(@PathVariable Integer id) {
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");
        return Result.success(act);
    }

    @Operation(summary = "Get completed activity data for content area")
    @GetMapping("/{id}/completed")
    public Result<Map<String, Object>> completed(@PathVariable Integer id) {
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");

        List<ActivityParticipation> participants = participationMapper.selectList(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, id)
                .eq(ActivityParticipation::getStatus, "COMPLETED")
                .orderByDesc(ActivityParticipation::getCompletedTime));

        List<Map<String, Object>> items = new ArrayList<>();
        for (ActivityParticipation p : participants) {
            ActivityDynamic dyn = dynamicMapper.selectOne(new LambdaQueryWrapper<ActivityDynamic>()
                    .eq(ActivityDynamic::getActivityId, id)
                    .eq(ActivityDynamic::getParticipationId, p.getId()));

            SysUser user = userMapper.selectById(p.getUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("participationId", p.getId());
            item.put("userId", p.getUserId());
            item.put("nickname", user != null ? user.getNickname() : null);
            item.put("completedTime", p.getCompletedTime());
            item.put("content", dyn != null ? dyn.getContent() : "");
            items.add(item);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("activityId", id);
        return Result.success(resp);
    }

    @Operation(summary = "Apply to an activity and generate required continuous tasks")
    @PostMapping("/{id}/apply")
    @Transactional
    public Result<Void> apply(@PathVariable Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");
        if (!"ONLINE".equals(act.getStatus())) return Result.error("Activity is not online");

        LocalDate today = LocalDate.now();
        if (act.getEndTime() != null && act.getEndTime().toLocalDate().isBefore(today)) {
            return Result.error("Activity has ended");
        }

        ActivityParticipation existing = participationMapper.selectOne(new LambdaQueryWrapper<ActivityParticipation>()
                .eq(ActivityParticipation::getActivityId, id)
                .eq(ActivityParticipation::getUserId, userId));
        if (existing != null) {
            return Result.success();
        }

        int requiredDays = act.getRequiredDays() != null ? act.getRequiredDays() : 7;
        if (requiredDays <= 0) requiredDays = 7;

        LocalDate startDate = act.getStartTime().toLocalDate();
        if (act.getEndTime() != null) {
            LocalDate lastDate = startDate.plusDays(requiredDays - 1L);
            if (lastDate.isAfter(act.getEndTime().toLocalDate())) {
                return Result.error("Activity duration is shorter than required days");
            }
        }

        ActivityParticipation participation = new ActivityParticipation();
        participation.setActivityId(id);
        participation.setUserId(userId);
        participation.setStatus("APPLIED");
        participation.setApplyTime(LocalDateTime.now());
        participationMapper.insert(participation);

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < requiredDays; i++) {
            LocalDate scheduledDate = startDate.plusDays(i);
            int dayIndex = i + 1;

            DailySchedule schedule = new DailySchedule();
            schedule.setUserId(userId);
            schedule.setDate(scheduledDate);
            schedule.setCreateTime(now);
            schedule.setStatus("PENDING");

            if ("PLAN".equals(act.getTemplateType())) {
                TrainingPlan plan = trainingMapper.selectById(act.getTemplateId());
                if (plan == null) throw new IllegalArgumentException("TrainingPlan not found");

                schedule.setSourceType("PLAN");
                schedule.setPlanId(act.getTemplateId());
                schedule.setTitle(act.getTitle() + " - 第 " + dayIndex + " 天训练");
                schedule.setDescription(plan.getDescription());
                schedule.setActions(plan.getActions());
            } else if ("COURSE".equals(act.getTemplateType())) {
                Course course = courseMapper.selectById(act.getTemplateId());
                if (course == null) throw new IllegalArgumentException("Course not found");

                schedule.setSourceType("COURSE");
                schedule.setCourseId(act.getTemplateId());
                schedule.setTitle(act.getTitle() + " - 第 " + dayIndex + " 天");
                schedule.setDescription(course.getDescription());
                schedule.setActions(course.getActionsJson());
            } else {
                throw new IllegalArgumentException("Unsupported templateType: " + act.getTemplateType());
            }

            dailyScheduleMapper.insert(schedule);

            ActivityTask task = new ActivityTask();
            task.setParticipationId(participation.getId());
            task.setDailyScheduleId(schedule.getId());
            task.setScheduledDate(scheduledDate);
            task.setTaskIndex(dayIndex);
            task.setStatus("PENDING");
            task.setCompletedTime(null);
            taskMapper.insert(task);
        }

        return Result.success();
    }
}

