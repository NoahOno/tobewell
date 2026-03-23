package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.DailySchedule;
import com.health.platform.entity.TrainingRecord;
import com.health.platform.mapper.DailyScheduleMapper;
import com.health.platform.mapper.TrainingRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
}
