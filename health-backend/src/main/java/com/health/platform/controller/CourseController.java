package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.Course;
import com.health.platform.entity.TrainingRecord;
import com.health.platform.mapper.CourseMapper;
import com.health.platform.mapper.TrainingRecordMapper;
import com.health.platform.service.ActivityProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Courses")
@RestController
@RequestMapping("/course")
@SaCheckLogin
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private com.health.platform.mapper.DailyScheduleMapper scheduleMapper;
    
    @Autowired
    private TrainingRecordMapper recordMapper;

    @Autowired
    private ActivityProgressService activityProgressService;

    public static class ScheduleCourseReq {
        private Integer courseId;
        private List<String> dates;
        public Integer getCourseId() { return courseId; }
        public void setCourseId(Integer courseId) { this.courseId = courseId; }
        public List<String> getDates() { return dates; }
        public void setDates(List<String> dates) { this.dates = dates; }
    }

    @Operation(summary = "Get public course library")
    @GetMapping("/library")
    public Result<List<Course>> getLibrary(@RequestParam(value = "keyword", required = false) String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublic, true)
                .orderByDesc(Course::getCreateTime);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Course::getTitle, keyword)
                    .or().like(Course::getDescription, keyword)
                    .or().like(Course::getCategory, keyword));
        }

        return Result.success(courseMapper.selectList(wrapper));
    }

    @Operation(summary = "Get my subscribed/created courses")
    @GetMapping("/my")
    public Result<List<Course>> getMyCourses() {
        Integer userId = StpUtil.getLoginIdAsInt();
        return Result.success(courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getCreatorId, userId)
                .eq(Course::getIsPublic, false)
                .orderByDesc(Course::getCreateTime)));
    }

    @Operation(summary = "Create or update my private course")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody Course course) {
        Integer userId = StpUtil.getLoginIdAsInt();

        if (course.getId() == null) {
            course.setCreatorId(userId);
            course.setIsPublic(false);
            course.setCreateTime(java.time.LocalDateTime.now());
            courseMapper.insert(course);
            return Result.success();
        }

        Course old = courseMapper.selectById(course.getId());
        if (old == null) return Result.error("Course not found");
        if (!userId.equals(old.getCreatorId()) && !StpUtil.hasRole("ADMIN")) {
            return Result.error("Permission denied");
        }

        course.setCreatorId(old.getCreatorId());
        if (!StpUtil.hasRole("ADMIN")) {
            course.setIsPublic(false);
        }
        if (course.getCreateTime() == null) {
            course.setCreateTime(old.getCreateTime());
        }
        courseMapper.updateById(course);
        return Result.success();
    }

    @Operation(summary = "Subscribe to a course")
    @PostMapping("/subscribe/{id}")
    public Result<Integer> subscribe(@PathVariable("id") Integer id) {
        Course original = courseMapper.selectById(id);
        if (original == null) {
            return Result.error("Course not found");
        }

        Integer userId = StpUtil.getLoginIdAsInt();

        // Check if already subscribed (clone by title)
        Course existing = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getCreatorId, userId)
                .eq(Course::getTitle, original.getTitle())
                .eq(Course::getIsPublic, false)
                .last("LIMIT 1"));

        if (existing != null && existing.getId() != null) {
            return Result.success(existing.getId()); // Already subscribed
        }

        Course clone = new Course();
        clone.setTitle(original.getTitle());
        clone.setDescription(original.getDescription());
        clone.setCategory(original.getCategory());
        clone.setDifficulty(original.getDifficulty());
        clone.setDurationMinutes(original.getDurationMinutes());
        clone.setActionsJson(original.getActionsJson());
        clone.setCoverImage(original.getCoverImage());
        clone.setIsPublic(false);
        clone.setCreatorId(userId);

        courseMapper.insert(clone);
        return Result.success(clone.getId());
    }

    @Operation(summary = "Unsubscribe or delete course")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable("id") Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        Course course = courseMapper.selectById(id);
        
        if (course != null && userId.equals(course.getCreatorId())) {
            // Remove associated pending schedules
            scheduleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.health.platform.entity.DailySchedule>()
                    .eq(com.health.platform.entity.DailySchedule::getCourseId, id)
                    .eq(com.health.platform.entity.DailySchedule::getSourceType, "COURSE")
                    .eq(com.health.platform.entity.DailySchedule::getStatus, "PENDING"));
            courseMapper.deleteById(id);
        }
        return Result.success();
    }

    public static class RecordReq {
        private Integer completeDuration;
        private String difficulty;
        private String feeling;

        public Integer getCompleteDuration() { return completeDuration; }
        public void setCompleteDuration(Integer completeDuration) { this.completeDuration = completeDuration; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getFeeling() { return feeling; }
        public void setFeeling(String feeling) { this.feeling = feeling; }
    }

    @Operation(summary = "Complete a course independent of a plan")
    @PostMapping("/{id}/complete")
    public Result<Void> completeCourse(@PathVariable("id") Integer id, @RequestBody RecordReq req) {
        Integer userId = StpUtil.getLoginIdAsInt();
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.error("Course not found");
        }

        TrainingRecord record = new TrainingRecord();
        record.setUserId(userId);
        record.setSourceType("COURSE");
        record.setSourceId(id);
        record.setCompleteDuration(req.getCompleteDuration() != null ? req.getCompleteDuration() : course.getDurationMinutes());
        record.setDifficulty(req.getDifficulty() != null ? req.getDifficulty() : "GOOD");
        record.setFeeling(req.getFeeling());

        recordMapper.insert(record);
        activityProgressService.refreshChallengeParticipationsForUser(userId);
        return Result.success();
    }
}
