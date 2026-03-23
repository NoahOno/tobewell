package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.Course;
import com.health.platform.entity.TrainingRecord;
import com.health.platform.mapper.CourseMapper;
import com.health.platform.mapper.TrainingRecordMapper;
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

    @Operation(summary = "Get public course library")
    @GetMapping("/library")
    public Result<List<Course>> getLibrary(@RequestParam(required = false) String keyword) {
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

    @Operation(summary = "Subscribe to a course")
    @PostMapping("/subscribe/{id}")
    public Result<Void> subscribe(@PathVariable Integer id) {
        Course original = courseMapper.selectById(id);
        if (original == null) {
            return Result.error("Course not found");
        }

        Integer userId = StpUtil.getLoginIdAsInt();

        // Check if already subscribed
        Long count = courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getCreatorId, userId)
                .eq(Course::getTitle, original.getTitle())
                .eq(Course::getIsPublic, false));

        if (count > 0) {
            return Result.success(); // Already subscribed
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
        return Result.success();
    }

    @Operation(summary = "Unsubscribe or delete course")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Integer id) {
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

    @Operation(summary = "Complete a course independent of a plan")
    @PostMapping("/{id}/complete")
    public Result<Void> completeCourse(@PathVariable Integer id, @RequestBody RecordReq req) {
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
        return Result.success();
    }
}
