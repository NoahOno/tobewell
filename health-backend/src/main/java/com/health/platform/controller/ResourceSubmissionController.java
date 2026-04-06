package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.Course;
import com.health.platform.entity.ResourceSubmission;
import com.health.platform.entity.TrainingPlan;
import com.health.platform.mapper.CourseMapper;
import com.health.platform.mapper.ResourceSubmissionMapper;
import com.health.platform.mapper.TrainingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Resource Submission")
@RestController
@RequestMapping("/resource")
@SaCheckLogin
public class ResourceSubmissionController {

    @Autowired
    private ResourceSubmissionMapper submissionMapper;

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private CourseMapper courseMapper;

    public static class SubmitReq {
        private String resourceType;
        private Integer resourceId;
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public Integer getResourceId() { return resourceId; }
        public void setResourceId(Integer resourceId) { this.resourceId = resourceId; }
    }

    public static class ReviewReq {
        private String note;
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    @Operation(summary = "Submit a private resource to public library (admin review required)")
    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody SubmitReq req) {
        Integer userId = StpUtil.getLoginIdAsInt();
        if (req == null || req.getResourceId() == null || req.getResourceType() == null) {
            return Result.error("Invalid request");
        }

        String type = req.getResourceType().toUpperCase();
        if (!("PLAN".equals(type) || "COURSE".equals(type))) {
            return Result.error("Invalid resourceType");
        }

        if ("PLAN".equals(type)) {
            TrainingPlan plan = trainingMapper.selectById(req.getResourceId());
            if (plan == null || !userId.equals(plan.getUserId())) return Result.error("Plan not found or unauthorized");
            if (Boolean.TRUE.equals(plan.getIsPublic())) return Result.error("Plan is already public");
        } else {
            Course course = courseMapper.selectById(req.getResourceId());
            if (course == null || !userId.equals(course.getCreatorId())) return Result.error("Course not found or unauthorized");
            if (Boolean.TRUE.equals(course.getIsPublic())) return Result.error("Course is already public");
        }

        ResourceSubmission existed = submissionMapper.selectOne(new LambdaQueryWrapper<ResourceSubmission>()
                .eq(ResourceSubmission::getSubmitterId, userId)
                .eq(ResourceSubmission::getResourceType, type)
                .eq(ResourceSubmission::getResourceId, req.getResourceId())
                .eq(ResourceSubmission::getStatus, "PENDING")
                .last("limit 1"));
        if (existed != null) return Result.success();

        ResourceSubmission sub = new ResourceSubmission();
        sub.setSubmitterId(userId);
        sub.setResourceType(type);
        sub.setResourceId(req.getResourceId());
        sub.setStatus("PENDING");
        sub.setCreateTime(LocalDateTime.now());
        submissionMapper.insert(sub);
        return Result.success();
    }

    @Operation(summary = "Get my submissions")
    @GetMapping("/my-submissions")
    public Result<List<ResourceSubmission>> mySubmissions(@RequestParam(value = "status", required = false) String status) {
        Integer userId = StpUtil.getLoginIdAsInt();
        LambdaQueryWrapper<ResourceSubmission> w = new LambdaQueryWrapper<ResourceSubmission>()
                .eq(ResourceSubmission::getSubmitterId, userId)
                .orderByDesc(ResourceSubmission::getCreateTime);
        if (status != null && !status.isEmpty()) w.eq(ResourceSubmission::getStatus, status);
        return Result.success(submissionMapper.selectList(w));
    }

    @Operation(summary = "Admin: list submissions")
    @SaCheckRole("ADMIN")
    @GetMapping("/admin/submissions")
    public Result<List<ResourceSubmission>> list(@RequestParam(value = "status", required = false) String status) {
        LambdaQueryWrapper<ResourceSubmission> w = new LambdaQueryWrapper<ResourceSubmission>()
                .orderByDesc(ResourceSubmission::getCreateTime);
        if (status != null && !status.isEmpty()) w.eq(ResourceSubmission::getStatus, status);
        return Result.success(submissionMapper.selectList(w));
    }

    @Operation(summary = "Admin: approve submission")
    @SaCheckRole("ADMIN")
    @PostMapping("/admin/submissions/{id}/approve")
    public Result<Void> approve(@PathVariable("id") Integer id) {
        Integer adminId = StpUtil.getLoginIdAsInt();
        ResourceSubmission sub = submissionMapper.selectById(id);
        if (sub == null) return Result.error("Submission not found");
        if (!"PENDING".equals(sub.getStatus())) return Result.success();

        if ("PLAN".equals(sub.getResourceType())) {
            TrainingPlan plan = trainingMapper.selectById(sub.getResourceId());
            if (plan != null) {
                plan.setIsPublic(true);
                trainingMapper.updateById(plan);
            }
        } else if ("COURSE".equals(sub.getResourceType())) {
            Course course = courseMapper.selectById(sub.getResourceId());
            if (course != null) {
                course.setIsPublic(true);
                courseMapper.updateById(course);
            }
        }

        sub.setStatus("APPROVED");
        sub.setReviewerId(adminId);
        sub.setReviewTime(LocalDateTime.now());
        submissionMapper.updateById(sub);
        return Result.success();
    }

    @Operation(summary = "Admin: reject submission")
    @SaCheckRole("ADMIN")
    @PostMapping("/admin/submissions/{id}/reject")
    public Result<Void> reject(@PathVariable("id") Integer id, @RequestBody(required = false) ReviewReq req) {
        Integer adminId = StpUtil.getLoginIdAsInt();
        ResourceSubmission sub = submissionMapper.selectById(id);
        if (sub == null) return Result.error("Submission not found");
        if (!"PENDING".equals(sub.getStatus())) return Result.success();

        sub.setStatus("REJECTED");
        sub.setReviewerId(adminId);
        sub.setReviewTime(LocalDateTime.now());
        sub.setNote(req != null ? req.getNote() : null);
        submissionMapper.updateById(sub);
        return Result.success();
    }
}

