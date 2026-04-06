package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.*;
import com.health.platform.mapper.*;
import com.health.platform.service.ActivityProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "Community Activities")
@RestController
@RequestMapping("/activity")
@SaCheckLogin
public class ActivityController {

    @Autowired private ActivityMapper activityMapper;
    @Autowired private ActivityParticipationMapper participationMapper;
    @Autowired private ActivityTaskMapper taskMapper;
    @Autowired private ActivityDynamicMapper dynamicMapper;
    @Autowired private TrainingMapper trainingMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private DailyScheduleMapper dailyScheduleMapper;
    @Autowired private ActivityProgressService activityProgressService;

    private static final ObjectMapper JSON = new ObjectMapper();

    @Operation(summary = "Get all online activities with participation status")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        Integer userId = StpUtil.getLoginIdAsInt();
        List<Activity> activities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, "ONLINE")
                .orderByDesc(Activity::getPinned)
                .orderByAsc(Activity::getStartTime));

    List<Map<String, Object>> result = new ArrayList<>();
        for (Activity a : activities) {
            ActivityParticipation part = participationMapper.selectOne(
                    new LambdaQueryWrapper<ActivityParticipation>()
                            .eq(ActivityParticipation::getActivityId, a.getId())
                            .eq(ActivityParticipation::getUserId, userId));

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", a.getId());
            map.put("title", a.getTitle());
            map.put("coverImage", a.getCoverImage());
            map.put("description", a.getDescriptionHtml());
            map.put("startTime", a.getStartTime());
            map.put("endTime", a.getEndTime());
            map.put("activityType", a.getActivityType());
            map.put("templateType", a.getTemplateType());
            map.put("templateId", a.getTemplateId());
            map.put("topicName", a.getTopicName());
            map.put("countMode", a.getCountMode());
            map.put("topicStatMode", a.getTopicStatMode());
            map.put("rewardPoints", a.getRewardPoints());
            map.put("requiredDays", a.getRequiredDays());
            map.put("pinned", a.getPinned());
            map.put("status", a.getStatus());
            map.put("joined", part != null);
            map.put("participationStatus", part != null ? part.getStatus() : null);

            if (part != null) {
                long[] progress = activityProgressService.computeProgress(userId, a, part);
                map.put("completedTasks", progress[0]);
                map.put("totalTasks", progress[1]);
            }
            map.put("targetDescription", buildTargetDescription(a));
            result.add(map);
        }
        return Result.success(result);
    }

    @Operation(summary = "Get trending activities Top 3")
    @GetMapping("/trending")
    public Result<List<Activity>> trending() {
        List<Activity> list = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, "ONLINE")
                .orderByDesc(Activity::getPinned)
                .orderByAsc(Activity::getStartTime)
                .last("LIMIT 3"));
        return Result.success(list);
    }

    @Operation(summary = "Get activity detail with participation status")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable("id") Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");

        ActivityParticipation part = participationMapper.selectOne(
                new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getActivityId, id)
                        .eq(ActivityParticipation::getUserId, userId));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", act.getId());
        map.put("title", act.getTitle());
        map.put("coverImage", act.getCoverImage());
        map.put("description", act.getDescriptionHtml());
        map.put("startTime", act.getStartTime());
        map.put("endTime", act.getEndTime());
        map.put("activityType", act.getActivityType());
        map.put("templateType", act.getTemplateType());
        map.put("templateId", act.getTemplateId());
        map.put("topicName", act.getTopicName());
        map.put("countMode", act.getCountMode());
        map.put("topicStatMode", act.getTopicStatMode());
        map.put("rewardPoints", act.getRewardPoints());
        map.put("requiredDays", act.getRequiredDays());
        map.put("pinned", act.getPinned());
        map.put("status", act.getStatus());
        map.put("joined", part != null);
        map.put("participationStatus", part != null ? part.getStatus() : null);

        if (part != null) {
            long[] progress = activityProgressService.computeProgress(userId, act, part);
            map.put("completedTasks", progress[0]);
            map.put("totalTasks", progress[1]);
        }

        if (act.getActivityType() == 2 && act.getTemplateId() != null) {
            if ("PLAN".equals(act.getTemplateType())) {
                TrainingPlan plan = trainingMapper.selectById(act.getTemplateId());
                if (plan != null) {
                    Map<String, Object> templateInfo = new HashMap<>();
                    templateInfo.put("id", plan.getId());
                    templateInfo.put("title", plan.getTitle());
                    templateInfo.put("description", plan.getDescription());
                    templateInfo.put("category", plan.getCategory());
                    templateInfo.put("duration", plan.getDuration());
                    templateInfo.put("coverImage", plan.getCoverImage());
                    map.put("templateInfo", templateInfo);
                }
            } else if ("COURSE".equals(act.getTemplateType())) {
                Course course = courseMapper.selectById(act.getTemplateId());
                if (course != null) {
                    Map<String, Object> templateInfo = new HashMap<>();
                    templateInfo.put("id", course.getId());
                    templateInfo.put("title", course.getTitle());
                    templateInfo.put("description", course.getDescription());
                    templateInfo.put("category", course.getCategory());
                    templateInfo.put("difficulty", course.getDifficulty());
                    templateInfo.put("durationMinutes", course.getDurationMinutes());
                    templateInfo.put("coverImage", course.getCoverImage());
                    map.put("templateInfo", templateInfo);
                }
            }
        }
        map.put("targetDescription", buildTargetDescription(act));
        return Result.success(map);
    }

    @Operation(summary = "Get my participated activities with progress")
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my() {
        Integer userId = StpUtil.getLoginIdAsInt();
        List<ActivityParticipation> myParts = participationMapper.selectList(
                new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getUserId, userId)
                        .orderByDesc(ActivityParticipation::getApplyTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ActivityParticipation p : myParts) {
            Activity act = activityMapper.selectById(p.getActivityId());
            if (act == null) continue;

            long[] progress = activityProgressService.computeProgress(p.getUserId(), act, p);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("participationId", p.getId());
            map.put("activityId", act.getId());
            map.put("title", act.getTitle());
            map.put("coverImage", act.getCoverImage());
            map.put("activityType", act.getActivityType());
            map.put("requiredDays", act.getRequiredDays());
            map.put("countMode", act.getCountMode());
            map.put("topicStatMode", act.getTopicStatMode());
            map.put("startTime", act.getStartTime());
            map.put("endTime", act.getEndTime());
            map.put("participationStatus", p.getStatus());
            map.put("applyTime", p.getApplyTime());
            map.put("completedTasks", progress[0]);
            map.put("totalTasks", progress[1]);
            map.put("targetDescription", buildTargetDescription(act));
            result.add(map);
        }
        return Result.success(result);
    }

    @Operation(summary = "Get completed participants for an activity")
    @GetMapping("/{id}/completed")
    public Result<Map<String, Object>> completed(@PathVariable("id") Integer id) {
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");

        List<ActivityParticipation> participants = participationMapper.selectList(
                new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getActivityId, id)
                        .orderByDesc(ActivityParticipation::getApplyTime));

        List<Map<String, Object>> items = new ArrayList<>();
        for (ActivityParticipation p : participants) {
            SysUser user = userMapper.selectById(p.getUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("participationId", p.getId());
            item.put("userId", p.getUserId());
            item.put("nickname", user != null ? user.getNickname() : null);
            item.put("status", p.getStatus());
            item.put("applyTime", p.getApplyTime());
            items.add(item);
        }

        long totalParticipants = participationMapper.selectCount(
                new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getActivityId, id));

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("totalParticipants", totalParticipants);
        return Result.success(resp);
    }

    private String buildTargetDescription(Activity a) {
        String countMode = a.getCountMode() != null ? a.getCountMode() : "DAYS";
        if (a.getActivityType() == 1) {
            String unit = "COUNT".equals(countMode) ? "次" : "天";
            return "打卡 " + a.getRequiredDays() + unit;
        } else if (a.getActivityType() == 2) {
            String unit = "COUNT".equals(countMode) ? "次" : "天";
            String title = "";
            if ("PLAN".equals(a.getTemplateType()) && a.getTemplateId() != null) {
                TrainingPlan plan = trainingMapper.selectById(a.getTemplateId());
                if (plan != null) title = plan.getTitle();
            } else if ("COURSE".equals(a.getTemplateType()) && a.getTemplateId() != null) {
                Course course = courseMapper.selectById(a.getTemplateId());
                if (course != null) title = course.getTitle();
            }
            if (title.isEmpty()) title = "训练计划或课程";
            return "完成 " + title + " " + a.getRequiredDays() + unit;
        } else if (a.getActivityType() == 3) {
            String topicName = a.getTopicName() != null ? a.getTopicName().trim() : "";
            if (!topicName.startsWith("#") && !topicName.isEmpty()) topicName = "#" + topicName;
            String statMode = a.getTopicStatMode() != null ? a.getTopicStatMode() : "COUNT";
            if ("SHARED".equals(statMode)) {
                return "分享 " + topicName + " 帖子";
            } else if ("DAYS".equals(statMode)) {
                return "分享 " + a.getRequiredDays() + "天 " + topicName + " 帖子";
            } else {
                return "分享 " + a.getRequiredDays() + "次 " + topicName + " 帖子";
            }
        }
        return "";
    }

    public static class ApplyReq {
        private String scheduleType; // "AUTO" or "FREE"
        private List<String> weeklyDays;

        public String getScheduleType() { return scheduleType; }
        public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }
        public List<String> getWeeklyDays() { return weeklyDays; }
        public void setWeeklyDays(List<String> weeklyDays) { this.weeklyDays = weeklyDays; }
    }

    @Operation(summary = "Join an activity")
    @PostMapping("/{id}/apply")
    @Transactional
    public Result<Void> apply(@PathVariable("id") Integer id, @RequestBody(required = false) ApplyReq req) {
        Integer userId = StpUtil.getLoginIdAsInt();
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");
        if (!"ONLINE".equals(act.getStatus())) return Result.error("Activity is not online");

        LocalDate today = LocalDate.now();
        if (act.getEndTime() != null && act.getEndTime().toLocalDate().isBefore(today)) {
            return Result.error("Activity has ended");
        }

        ActivityParticipation existing = participationMapper.selectOne(
                new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getActivityId, id)
                        .eq(ActivityParticipation::getUserId, userId));
        if (existing != null) return Result.success();

        ActivityParticipation participation = new ActivityParticipation();
        participation.setActivityId(id);
        participation.setUserId(userId);
        participation.setStatus("APPLIED");
        participation.setApplyTime(LocalDateTime.now());
        participationMapper.insert(participation);

        String scheduleType = "AUTO";
        if (req != null && req.getScheduleType() != null && !req.getScheduleType().isBlank()) {
            scheduleType = req.getScheduleType().trim();
        }
        if (act.getActivityType() == 2 && act.getTemplateId() != null && "AUTO".equalsIgnoreCase(scheduleType)) {
            List<String> weeklyDays = req != null && req.getWeeklyDays() != null ? req.getWeeklyDays() : null;
            generateActivityTasks(participation.getId(), act, today, userId, weeklyDays);
        }

        return Result.success();
    }

    /**
     * 为挑战类「自动安排」在 daily_schedule 中创建真实训练项，并关联 activity_task，日历与完成记录才能打通。
     */
    private void generateActivityTasks(Integer participationId, Activity act, LocalDate startDate, Integer userId, List<String> weeklyDays) {
        if (weeklyDays == null || weeklyDays.isEmpty()) {
            weeklyDays = List.of("MONDAY", "WEDNESDAY", "FRIDAY");
        }
        Integer requiredDays = act.getRequiredDays() != null ? act.getRequiredDays() : 7;
        TrainingPlan userPlan = null;
        List<Map<String, Object>> planDays = Collections.emptyList();
        if ("PLAN".equals(act.getTemplateType()) && act.getTemplateId() != null) {
            userPlan = ensureUserPlanCopyForActivity(userId, act.getTemplateId());
            if (userPlan != null && userPlan.getActions() != null && !userPlan.getActions().isBlank()) {
                try {
                    planDays = JSON.readValue(userPlan.getActions(), new TypeReference<List<Map<String, Object>>>() {});
                } catch (Exception e) {
                    planDays = Collections.emptyList();
                }
            }
        }
        Course templateCourse = null;
        if ("COURSE".equals(act.getTemplateType()) && act.getTemplateId() != null) {
            templateCourse = courseMapper.selectById(act.getTemplateId());
        }

        int dayOffset = 0;
        int planDayIdx = 0;
        for (int i = 0; i < requiredDays; ) {
            LocalDate day = startDate.plusDays(dayOffset);
            dayOffset++;
            String dayOfWeekName = day.getDayOfWeek().name();
            if (!weeklyDays.contains(dayOfWeekName)) {
                continue;
            }
            Integer linkedScheduleId = null;

            if ("COURSE".equals(act.getTemplateType()) && templateCourse != null) {
                DailySchedule sch = new DailySchedule();
                sch.setUserId(userId);
                sch.setDate(day);
                sch.setSourceType("COURSE");
                sch.setPlanId(null);
                sch.setCourseId(templateCourse.getId());
                sch.setTitle("[活动挑战] " + templateCourse.getTitle());
                sch.setDescription(templateCourse.getDescription());
                sch.setActions(templateCourse.getActionsJson());
                sch.setStatus("PENDING");
                sch.setCreateTime(LocalDateTime.now());
                dailyScheduleMapper.insert(sch);
                linkedScheduleId = sch.getId();
            } else if ("PLAN".equals(act.getTemplateType()) && userPlan != null) {
                DailySchedule sch = new DailySchedule();
                sch.setUserId(userId);
                sch.setDate(day);
                sch.setSourceType("PLAN");
                sch.setPlanId(userPlan.getId());
                sch.setStatus("PENDING");
                sch.setCreateTime(LocalDateTime.now());
                Map<String, Object> dayCfg = planDays.isEmpty() ? null : planDays.get(i % planDays.size());
                Integer cid = null;
                if (dayCfg != null && dayCfg.get("courseId") != null) {
                    cid = ((Number) dayCfg.get("courseId")).intValue();
                }
                if (cid != null) {
                    Course c = courseMapper.selectById(cid);
                    sch.setCourseId(cid);
                    sch.setTitle(c != null ? "[活动挑战] " + c.getTitle() : "[活动挑战] 训练日 " + (i + 1));
                    sch.setDescription(c != null ? c.getDescription() : "");
                    sch.setActions(c != null ? c.getActionsJson() : null);
                } else {
                    sch.setCourseId(null);
                    String t = (dayCfg != null && dayCfg.get("title") != null)
                            ? dayCfg.get("title").toString()
                            : ("训练日 " + (i + 1));
                    sch.setTitle("[活动挑战] " + t);
                    sch.setDescription("");
                    sch.setActions(null);
                }
                dailyScheduleMapper.insert(sch);
                linkedScheduleId = sch.getId();
            }

            ActivityTask task = new ActivityTask();
            task.setParticipationId(participationId);
            task.setTaskDate(day);
            task.setScheduledDate(day);
            task.setTaskIndex(i);
            task.setDailyScheduleId(linkedScheduleId != null ? linkedScheduleId : -(participationId * 50_000 + i + 1));
            task.setStatus("PENDING");
            task.setTaskType(act.getTemplateType());
            task.setTaskId(act.getTemplateId());
            taskMapper.insert(task);
            planDayIdx++;
            i++;
        }
    }

    private TrainingPlan ensureUserPlanCopyForActivity(Integer userId, Integer templatePlanId) {
        TrainingPlan template = trainingMapper.selectById(templatePlanId);
        if (template == null) {
            return null;
        }
        if (userId.equals(template.getUserId())) {
            return template;
        }
        TrainingPlan existing = trainingMapper.selectOne(
                new LambdaQueryWrapper<TrainingPlan>()
                        .eq(TrainingPlan::getUserId, userId)
                        .eq(TrainingPlan::getSourceId, templatePlanId)
                        .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        TrainingPlan clone = new TrainingPlan();
        clone.setUserId(userId);
        clone.setTitle(template.getTitle());
        clone.setDescription(template.getDescription());
        clone.setContent(template.getContent());
        clone.setDuration(template.getDuration());
        clone.setActions(template.getActions());
        clone.setCategory(template.getCategory());
        clone.setStartDate(LocalDate.now());
        clone.setEndDate(template.getEndDate());
        clone.setIsPublic(false);
        clone.setSourceId(templatePlanId);
        clone.setIsSubscribed(true);
        clone.setStatus("ACTIVE");
        trainingMapper.insert(clone);
        return clone;
    }

    @Operation(summary = "Get activity tasks for current user")
    @GetMapping("/{id}/tasks")
    public Result<List<ActivityTask>> getTasks(@PathVariable("id") Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        ActivityParticipation part = participationMapper.selectOne(
                new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getActivityId, id)
                        .eq(ActivityParticipation::getUserId, userId));
        if (part == null) return Result.error("Not participated in this activity");
        List<ActivityTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<ActivityTask>()
                        .eq(ActivityTask::getParticipationId, part.getId())
                        .orderByAsc(ActivityTask::getTaskDate));
        return Result.success(tasks);
    }

    @Operation(summary = "Check in for an activity task")
    @PostMapping("/{id}/checkin")
    @Transactional
    public Result<Void> checkIn(@PathVariable("id") Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        Activity act = activityMapper.selectById(id);
        if (act == null) return Result.error("Activity not found");

        ActivityParticipation part = participationMapper.selectOne(
                new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getActivityId, id)
                        .eq(ActivityParticipation::getUserId, userId));
        if (part == null) return Result.error("Not participated in this activity");

        LocalDate today = LocalDate.now();

        if (act.getActivityType() == 1) {
            String countMode = act.getCountMode() != null ? act.getCountMode() : "DAYS";
            if ("COUNT".equals(countMode)) {
                long seq = taskMapper.selectCount(new LambdaQueryWrapper<ActivityTask>()
                        .eq(ActivityTask::getParticipationId, part.getId()));
                ActivityTask task = new ActivityTask();
                task.setParticipationId(part.getId());
                task.setTaskDate(today);
                task.setScheduledDate(today);
                task.setTaskIndex((int) seq);
                task.setDailyScheduleId(-(part.getId() * 50_000 + 10_000 + (int) seq + 1));
                task.setStatus("COMPLETED");
                task.setCompletedTime(LocalDateTime.now());
                task.setTaskType("CHECKIN");
                taskMapper.insert(task);
            } else {
                ActivityTask existingTask = taskMapper.selectOne(
                        new LambdaQueryWrapper<ActivityTask>()
                                .eq(ActivityTask::getParticipationId, part.getId())
                                .eq(ActivityTask::getTaskDate, today));
                if (existingTask != null) {
                    if ("COMPLETED".equals(existingTask.getStatus())) {
                        return Result.error("今天已打卡，请勿重复打卡");
                    }
                    existingTask.setStatus("COMPLETED");
                    existingTask.setCompletedTime(LocalDateTime.now());
                    taskMapper.updateById(existingTask);
                } else {
                    long seq = taskMapper.selectCount(new LambdaQueryWrapper<ActivityTask>()
                            .eq(ActivityTask::getParticipationId, part.getId()));
                    ActivityTask task = new ActivityTask();
                    task.setParticipationId(part.getId());
                    task.setTaskDate(today);
                    task.setScheduledDate(today);
                    task.setTaskIndex((int) seq);
                    task.setDailyScheduleId(-(part.getId() * 50_000 + 10_000 + (int) seq + 1));
                    task.setStatus("COMPLETED");
                    task.setCompletedTime(LocalDateTime.now());
                    task.setTaskType("CHECKIN");
                    taskMapper.insert(task);
                }
            }
        } else if (act.getActivityType() == 3) {
            return Result.error("话题类活动请通过发布话题帖参与，无需手动打卡");
        } else if (act.getActivityType() == 2) {
            return Result.error("挑战类活动无需手动打卡，系统会根据您的训练记录自动统计");
        }

        activityProgressService.syncParticipationStatus(part.getId());
        return Result.success();
    }

    @Operation(summary = "Quit an activity")
    @PostMapping("/{id}/quit")
    @Transactional
    public Result<Void> quit(@PathVariable("id") Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        ActivityParticipation part = participationMapper.selectOne(
                new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getActivityId, id)
                        .eq(ActivityParticipation::getUserId, userId));
        if (part == null) {
            return Result.error("Not participated in this activity");
        }

        List<ActivityTask> linked = taskMapper.selectList(
                new LambdaQueryWrapper<ActivityTask>().eq(ActivityTask::getParticipationId, part.getId()));
        for (ActivityTask t : linked) {
            Integer sid = t.getDailyScheduleId();
            if (sid != null && sid > 0) {
                dailyScheduleMapper.deleteById(sid);
            }
        }
        taskMapper.delete(new LambdaQueryWrapper<ActivityTask>().eq(ActivityTask::getParticipationId, part.getId()));
        participationMapper.deleteById(part.getId());

        return Result.success();
    }
}
