package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.Activity;
import com.health.platform.entity.ActivityDynamic;
import com.health.platform.entity.ActivityParticipation;
import com.health.platform.entity.ActivityTask;
import com.health.platform.entity.DailySchedule;
import com.health.platform.entity.SysUser;
import com.health.platform.entity.TrainingPlan;
import com.health.platform.entity.TrainingRecord;
import com.health.platform.mapper.DailyScheduleMapper;
import com.health.platform.mapper.ActivityDynamicMapper;
import com.health.platform.mapper.ActivityMapper;
import com.health.platform.mapper.ActivityParticipationMapper;
import com.health.platform.mapper.ActivityTaskMapper;
import com.health.platform.mapper.TrainingRecordMapper;
import com.health.platform.mapper.TrainingMapper;
import com.health.platform.mapper.UserMapper;
import com.health.platform.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Tag(name = "Daily Schedules & Records")
@RestController
@RequestMapping("/daily")
@SaCheckLogin
public class DailyScheduleController {

    @Autowired
    private DailyScheduleMapper scheduleMapper;

    @Autowired
    private TrainingRecordMapper recordMapper;

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityParticipationMapper participationMapper;

    @Autowired
    private ActivityTaskMapper taskMapper;

    @Autowired
    private ActivityDynamicMapper dynamicMapper;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "Get today's schedules")
    @GetMapping("/today")
    public Result<List<DailySchedule>> getTodaySchedules() {
        Integer userId = StpUtil.getLoginIdAsInt();
        LocalDate today = LocalDate.now();
        List<DailySchedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<DailySchedule>()
                .eq(DailySchedule::getUserId, userId)
                .eq(DailySchedule::getDate, today)
                .orderByAsc(DailySchedule::getCreateTime));
        return Result.success(schedules);
    }
    
    @Operation(summary = "Get schedules by date range")
    @GetMapping("/range")
    public Result<List<DailySchedule>> getSchedulesInRange(@RequestParam String start, @RequestParam String end) {
        Integer userId = StpUtil.getLoginIdAsInt();
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        
        List<DailySchedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<DailySchedule>()
                .eq(DailySchedule::getUserId, userId)
                .between(DailySchedule::getDate, startDate, endDate)
                .orderByAsc(DailySchedule::getDate));
        return Result.success(schedules);
    }

    public static class CheckInReq {
        private Integer completeDuration;
        private String difficulty; // TOO_EASY, GOOD, TOO_HARD
        private String feeling;
        private String status;

        public Integer getCompleteDuration() { return completeDuration; }
        public void setCompleteDuration(Integer completeDuration) { this.completeDuration = completeDuration; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getFeeling() { return feeling; }
        public void setFeeling(String feeling) { this.feeling = feeling; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @Operation(summary = "Complete a daily schedule and submit feedback")
    @PostMapping("/{id}/complete")
    public Result<Void> completeSchedule(@PathVariable Integer id, @RequestBody CheckInReq req) {
        Integer userId = StpUtil.getLoginIdAsInt();
        DailySchedule schedule = scheduleMapper.selectById(id);
        
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return Result.error("Schedule not found or unauthorized");
        }
        
        // Only update status if explicitly provided, else remain PENDING/unchanged
        if (req.getStatus() != null) {
            schedule.setStatus(req.getStatus());
            scheduleMapper.updateById(schedule);
        }

        TrainingRecord record = new TrainingRecord();
        record.setUserId(userId);
        record.setSourceType("SCHEDULE");
        record.setSourceId(id);
        record.setCompleteDuration(req.getCompleteDuration() != null ? req.getCompleteDuration() : 30);
        record.setDifficulty(req.getDifficulty() != null ? req.getDifficulty() : "GOOD");
        record.setFeeling(req.getFeeling());
        record.setRecordTime(java.time.LocalDateTime.now());
        recordMapper.insert(record);

        // Activity integration: mark task completion and generate activity dynamic when all tasks finished.
        if ("COMPLETED".equals(schedule.getStatus())) {
            ActivityTask task = taskMapper.selectOne(new LambdaQueryWrapper<ActivityTask>()
                    .eq(ActivityTask::getDailyScheduleId, id));

            if (task != null && task.getParticipationId() != null) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                task.setStatus("COMPLETED");
                task.setCompletedTime(now);
                taskMapper.updateById(task);

                ActivityParticipation participation = participationMapper.selectById(task.getParticipationId());
                if (participation != null) {
                    List<ActivityTask> allTasks = taskMapper.selectList(new LambdaQueryWrapper<ActivityTask>()
                            .eq(ActivityTask::getParticipationId, participation.getId()));

                    boolean allCompleted = !allTasks.isEmpty()
                            && allTasks.stream().allMatch(t -> "COMPLETED".equals(t.getStatus()));

                    if (allCompleted) {
                        participation.setStatus("COMPLETED");
                        participation.setCompletedTime(now);
                        participationMapper.updateById(participation);

                        Activity act = activityMapper.selectById(participation.getActivityId());
                        SysUser user = userMapper.selectById(participation.getUserId());

                        ActivityDynamic existed = dynamicMapper.selectOne(new LambdaQueryWrapper<ActivityDynamic>()
                                .eq(ActivityDynamic::getActivityId, act != null ? act.getId() : participation.getActivityId())
                                .eq(ActivityDynamic::getParticipationId, participation.getId()));

                        if (existed == null && act != null) {
                            ActivityDynamic dyn = new ActivityDynamic();
                            dyn.setActivityId(act.getId());
                            dyn.setParticipationId(participation.getId());
                            dyn.setUserId(participation.getUserId());
                            // Content will be shown in activity content area and used as a template for forwarding.
                            String nickname = user != null && user.getNickname() != null ? user.getNickname() : ("用户#" + participation.getUserId());
                            dyn.setContent("[" + nickname + "] 已完成活动《" + act.getTitle() + "》！坚持训练，保持节奏。");
                            dynamicMapper.insert(dyn);
                        }
                    }
                }
            }
        }

        return Result.success();
    }

    @Operation(summary = "Submit feedback for a completed schedule (no status change)")
    @PostMapping("/{id}/feedback")
    public Result<Void> submitFeedback(@PathVariable Integer id, @RequestBody CheckInReq req) {
        Integer userId = StpUtil.getLoginIdAsInt();
        DailySchedule schedule = scheduleMapper.selectById(id);

        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return Result.error("Schedule not found or unauthorized");
        }

        // Only insert a training record — do NOT touch schedule status
        TrainingRecord record = new TrainingRecord();
        record.setUserId(userId);
        record.setSourceType("SCHEDULE");
        record.setSourceId(id);
        record.setCompleteDuration(req.getCompleteDuration() != null ? req.getCompleteDuration() : 1);
        record.setDifficulty(req.getDifficulty() != null ? req.getDifficulty() : "GOOD");
        record.setFeeling(req.getFeeling());
        record.setRecordTime(java.time.LocalDateTime.now());
        recordMapper.insert(record);

        return Result.success();
    }

    @Operation(summary = "Submit feedback without linking to a schedule (e.g. abort flow)")
    @PostMapping("/feedback")
    public Result<Void> submitGeneralFeedback(@RequestBody CheckInReq req) {
        Integer userId = StpUtil.getLoginIdAsInt();

        TrainingRecord record = new TrainingRecord();
        record.setUserId(userId);
        record.setSourceType("GENERAL");
        record.setSourceId(null);
        record.setCompleteDuration(req.getCompleteDuration() != null ? req.getCompleteDuration() : 1);
        record.setDifficulty(req.getDifficulty() != null ? req.getDifficulty() : "GOOD");
        record.setFeeling(req.getFeeling());
        record.setRecordTime(java.time.LocalDateTime.now());
        recordMapper.insert(record);

        return Result.success();
    }

    @Operation(summary = "Skip a daily schedule")
    @PostMapping("/{id}/skip")
    public Result<Void> skipSchedule(@PathVariable Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        DailySchedule schedule = scheduleMapper.selectById(id);
        
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return Result.error("Schedule not found or unauthorized");
        }
        
        if ("COMPLETED".equals(schedule.getStatus())) {
            return Result.error("Cannot skip a completed schedule");
        }

        schedule.setStatus("SKIPPED");
        scheduleMapper.updateById(schedule);
        return Result.success();
    }

    @Operation(summary = "Postpone a schedule to selected date")
    @PostMapping("/{id}/postpone")
    public Result<Void> postponeSchedule(@PathVariable Integer id, @RequestBody PostponeReq req) {
        Integer userId = StpUtil.getLoginIdAsInt();
        DailySchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return Result.error("Schedule not found or unauthorized");
        }
        if (!LocalDate.now().equals(schedule.getDate())) {
            return Result.error("Only today's schedule can be postponed");
        }
        if (!"PENDING".equals(schedule.getStatus())) {
            return Result.error("Only pending schedules can be postponed");
        }

        if (req == null || req.getTargetDate() == null) {
            return Result.error("targetDate is required");
        }
        if (!req.getTargetDate().isAfter(schedule.getDate())) {
            return Result.error("targetDate must be later than original date");
        }
        long deltaDays = ChronoUnit.DAYS.between(schedule.getDate(), req.getTargetDate());

        if ("COURSE".equals(schedule.getSourceType())) {
            schedule.setDate(schedule.getDate().plusDays(deltaDays));
            scheduleMapper.updateById(schedule);
            return Result.success();
        }

        if ("PLAN".equals(schedule.getSourceType()) && schedule.getPlanId() != null) {
            List<DailySchedule> tails = scheduleMapper.selectList(new LambdaQueryWrapper<DailySchedule>()
                    .eq(DailySchedule::getUserId, userId)
                    .eq(DailySchedule::getPlanId, schedule.getPlanId())
                    .eq(DailySchedule::getSourceType, "PLAN")
                    .eq(DailySchedule::getStatus, "PENDING")
                    .ge(DailySchedule::getDate, schedule.getDate())
                    .orderByDesc(DailySchedule::getDate));
            for (DailySchedule s : tails) {
                s.setDate(s.getDate().plusDays(deltaDays));
                scheduleMapper.updateById(s);
            }
            return Result.success();
        }

        return Result.error("Unsupported schedule type");
    }

    @Operation(summary = "Pause upcoming schedules")
    @PostMapping("/{id}/pause")
    public Result<Void> pauseUpcomingSchedules(@PathVariable Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        DailySchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return Result.error("Schedule not found or unauthorized");
        }

        LambdaQueryWrapper<DailySchedule> wrapper = new LambdaQueryWrapper<DailySchedule>()
                .eq(DailySchedule::getUserId, userId)
                .eq(DailySchedule::getStatus, "PENDING")
                .ge(DailySchedule::getDate, schedule.getDate());

        if ("PLAN".equals(schedule.getSourceType()) && schedule.getPlanId() != null) {
            wrapper.eq(DailySchedule::getSourceType, "PLAN")
                    .eq(DailySchedule::getPlanId, schedule.getPlanId());
        } else if ("COURSE".equals(schedule.getSourceType()) && schedule.getCourseId() != null) {
            wrapper.eq(DailySchedule::getSourceType, "COURSE")
                    .eq(DailySchedule::getCourseId, schedule.getCourseId());
        } else {
            return Result.error("Unsupported schedule type");
        }

        List<DailySchedule> targets = scheduleMapper.selectList(wrapper);
        for (DailySchedule s : targets) {
            s.setStatus("PAUSED");
            scheduleMapper.updateById(s);
        }
        return Result.success();
    }

    @Operation(summary = "Resume paused schedules")
    @PostMapping("/{id}/resume")
    public Result<Void> resumePausedSchedules(@PathVariable Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        DailySchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return Result.error("Schedule not found or unauthorized");
        }

        LambdaQueryWrapper<DailySchedule> wrapper = new LambdaQueryWrapper<DailySchedule>()
                .eq(DailySchedule::getUserId, userId)
                .eq(DailySchedule::getStatus, "PAUSED")
                .ge(DailySchedule::getDate, schedule.getDate());

        if ("PLAN".equals(schedule.getSourceType()) && schedule.getPlanId() != null) {
            wrapper.eq(DailySchedule::getSourceType, "PLAN")
                    .eq(DailySchedule::getPlanId, schedule.getPlanId());
        } else if ("COURSE".equals(schedule.getSourceType()) && schedule.getCourseId() != null) {
            wrapper.eq(DailySchedule::getSourceType, "COURSE")
                    .eq(DailySchedule::getCourseId, schedule.getCourseId());
        } else {
            return Result.error("Unsupported schedule type");
        }

        List<DailySchedule> targets = scheduleMapper.selectList(wrapper);
        for (DailySchedule s : targets) {
            s.setStatus("PENDING");
            scheduleMapper.updateById(s);
        }
        return Result.success();
    }

    @Operation(summary = "Reset plan progress and regenerate schedules")
    @PostMapping("/{id}/reset")
    public Result<Void> resetPlanProgress(@PathVariable Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        DailySchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return Result.error("Schedule not found or unauthorized");
        }

        // Activity tasks cannot be reset (they are managed by activity progress).
        ActivityTask activityTask = taskMapper.selectOne(new LambdaQueryWrapper<ActivityTask>()
                .eq(ActivityTask::getDailyScheduleId, id));
        if (activityTask != null) {
            return Result.error("Activity tasks cannot be reset");
        }

        if (!"PLAN".equals(schedule.getSourceType()) || schedule.getPlanId() == null) {
            return Result.error("Only plan schedules support reset");
        }

        TrainingPlan plan = trainingMapper.selectById(schedule.getPlanId());
        if (plan == null || !userId.equals(plan.getUserId())) {
            return Result.error("Plan not found or unauthorized");
        }

        plan.setStartDate(LocalDate.now());
        plan.setStatus("ACTIVE");
        trainingMapper.updateById(plan);
        scheduleService.generateSchedule(plan, null);
        return Result.success();
    }
    
    @Operation(summary = "Get user's training records")
    @GetMapping("/records")
    public Result<List<TrainingRecord>> getRecords() {
        Integer userId = StpUtil.getLoginIdAsInt();
        List<TrainingRecord> records = recordMapper.selectList(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .orderByDesc(TrainingRecord::getRecordTime));
        return Result.success(records);
    }

    public static class CourseScheduleReq {
        private Integer courseId;
        private List<LocalDate> dates;

        public Integer getCourseId() { return courseId; }
        public void setCourseId(Integer courseId) { this.courseId = courseId; }
        public List<LocalDate> getDates() { return dates; }
        public void setDates(List<LocalDate> dates) { this.dates = dates; }
    }

    public static class PostponeReq {
        private LocalDate targetDate;

        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    }

    @Autowired
    private com.health.platform.mapper.CourseMapper courseMapper;

    @Operation(summary = "Schedule a single course to specific dates")
    @PostMapping("/course")
    public Result<Void> scheduleCourse(@RequestBody CourseScheduleReq req) {
        Integer userId = StpUtil.getLoginIdAsInt();
        com.health.platform.entity.Course course = courseMapper.selectById(req.getCourseId());
        if (course == null) return Result.error("Course not found");

        for (LocalDate date : req.getDates()) {
            DailySchedule schedule = new DailySchedule();
            schedule.setUserId(userId);
            schedule.setCourseId(req.getCourseId());
            schedule.setSourceType("COURSE");
            schedule.setDate(date);
            schedule.setTitle(course.getTitle());
            schedule.setDescription(course.getDescription());
            schedule.setActions(course.getActionsJson());
            schedule.setStatus("PENDING");
            schedule.setCreateTime(java.time.LocalDateTime.now());
            scheduleMapper.insert(schedule);
        }
        return Result.success();
    }

    @Operation(summary = "Cancel one scheduled training item")
    @DeleteMapping("/{id}")
    public Result<Void> cancelScheduledItem(@PathVariable Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        DailySchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || !userId.equals(schedule.getUserId())) {
            return Result.error("Schedule not found or unauthorized");
        }
        if ("COMPLETED".equals(schedule.getStatus())) {
            return Result.error("Completed schedule cannot be canceled");
        }
        scheduleMapper.deleteById(id);
        return Result.success();
    }
}
