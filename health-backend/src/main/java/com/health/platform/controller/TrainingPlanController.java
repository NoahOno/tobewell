package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.TrainingPlan;
import com.health.platform.mapper.TrainingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;

@Tag(name = "Training Plans")
@RestController
@RequestMapping("/training")
@SaCheckLogin
public class TrainingPlanController {

    public static class SubscribeReq {
        private LocalDate startDate;
        private List<String> weeklyDays;
        private Boolean activate;

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public List<String> getWeeklyDays() { return weeklyDays; }
        public void setWeeklyDays(List<String> weeklyDays) { this.weeklyDays = weeklyDays; }
        public Boolean getActivate() { return activate; }
        public void setActivate(Boolean activate) { this.activate = activate; }
    }

    @Autowired
    private TrainingMapper trainingMapper;

    @Operation(summary = "Get user's training plans")
    @GetMapping("/list")
    public Result<List<TrainingPlan>> getPlans() {
        Integer userId = StpUtil.getLoginIdAsInt();
        return Result.success(trainingMapper.selectList(new LambdaQueryWrapper<TrainingPlan>()
                .eq(TrainingPlan::getUserId, userId)
                .orderByDesc(TrainingPlan::getStartDate)));
    }

    @Operation(summary = "Get current active training plan")
    @GetMapping("/current")
    public Result<TrainingPlan> getCurrentPlan() {
        Integer userId = StpUtil.getLoginIdAsInt();
        TrainingPlan currentPlan = trainingMapper.selectOne(new LambdaQueryWrapper<TrainingPlan>()
                .eq(TrainingPlan::getUserId, userId)
                .eq(TrainingPlan::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        return Result.success(currentPlan);
    }

    @Operation(summary = "Create or update a training plan")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody TrainingPlan plan) {
        Integer userId = StpUtil.getLoginIdAsInt();
        plan.setUserId(userId);
        
        if ("ACTIVE".equals(plan.getStatus())) {
            archiveActivePlans(userId);
        }

        if (plan.getId() == null) {
            trainingMapper.insert(plan);
        } else {
            trainingMapper.updateById(plan);
        }
        return Result.success();
    }

    private void archiveActivePlans(Integer userId) {
        List<TrainingPlan> activePlans = trainingMapper.selectList(new LambdaQueryWrapper<TrainingPlan>()
                .eq(TrainingPlan::getUserId, userId)
                .eq(TrainingPlan::getStatus, "ACTIVE"));
        for (TrainingPlan activePlan : activePlans) {
            activePlan.setStatus("ARCHIVED");
            trainingMapper.updateById(activePlan);
        }
    }

    @Operation(summary = "Delete a training plan permanently")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        TrainingPlan plan = trainingMapper.selectById(id);
        
        if (plan == null) {
            return Result.success();
        }
        
        // If it's a public plan (even if created by me), detach from user instead of deleting
        if (Boolean.TRUE.equals(plan.getIsPublic())) {
            // Only the owner can "delete" (detach) it from their view
            if (plan.getUserId().equals(userId)) {
                plan.setUserId(null); // Detach -> Becomes system plan
                trainingMapper.updateById(plan);
            }
        } else {
            // Private plan -> Hard delete (only if owner)
            if (plan.getUserId().equals(userId)) {
                trainingMapper.deleteById(id);
            }
        }
        return Result.success();
    }

    @Operation(summary = "Soft unsubscribe from a plan")
    @PostMapping("/unsubscribe/{id}")
    public Result<Void> unsubscribe(@PathVariable Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        TrainingPlan plan = trainingMapper.selectById(id);
        
        if (plan != null && userId.equals(plan.getUserId())) {
            plan.setIsSubscribed(false);
            trainingMapper.updateById(plan);
        }
        return Result.success();
    }

    @Operation(summary = "Get public training plans (Health Library)")
    @GetMapping("/library")
    public Result<List<TrainingPlan>> getLibrary(@RequestParam(required = false) String keyword, @RequestParam(required = false) String category) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<TrainingPlan>()
                .eq(TrainingPlan::getIsPublic, true);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TrainingPlan::getTitle, keyword)
                    .or().like(TrainingPlan::getDescription, keyword)
                    .or().like(TrainingPlan::getCategory, keyword));
        }
        
        if (category != null && !category.isEmpty()) {
            wrapper.like(TrainingPlan::getCategory, category);
        }
        
        wrapper.orderByDesc(TrainingPlan::getStartDate);
        return Result.success(trainingMapper.selectList(wrapper));
    }

    @Operation(summary = "Get all distinct categories from public plans")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        List<TrainingPlan> plans = trainingMapper.selectList(new LambdaQueryWrapper<TrainingPlan>()
                .eq(TrainingPlan::getIsPublic, true));
        
        return Result.success(plans.stream()
                .filter(p -> p.getCategory() != null)
                .flatMap(p -> java.util.Arrays.stream(p.getCategory().split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(java.util.stream.Collectors.toList()));
    }

    @Operation(summary = "Subscribe to a plan")
    @PostMapping("/subscribe/{id}")
    public Result<Void> subscribe(@PathVariable Integer id, @RequestBody(required = false) SubscribeReq req) {
        TrainingPlan original = trainingMapper.selectById(id);
        if (original == null) {
            return Result.error("Plan not found");
        }
        
        Integer userId = StpUtil.getLoginIdAsInt();
        
        // Check if user already has an instance of this plan
        TrainingPlan existing = trainingMapper.selectOne(new LambdaQueryWrapper<TrainingPlan>()
                .eq(TrainingPlan::getUserId, userId)
                .and(w -> w.eq(TrainingPlan::getSourceId, id).or().eq(TrainingPlan::getTitle, original.getTitle()))
                .last("LIMIT 1"));

        if (existing != null) {
            // Update existing instance
            existing.setIsSubscribed(true);
            if (req != null && Boolean.TRUE.equals(req.getActivate())) {
                archiveActivePlans(userId);
                existing.setStatus("ACTIVE");
            }
            if (req != null && req.getStartDate() != null) {
                existing.setStartDate(req.getStartDate());
            }
            trainingMapper.updateById(existing);
            return Result.success();
        }

        // Clone the plan if no existing instance
        TrainingPlan clone = new TrainingPlan();
        clone.setUserId(userId);
        clone.setTitle(original.getTitle());
        clone.setDescription(original.getDescription());
        clone.setContent(original.getContent()); // Copy content
        clone.setDuration(original.getDuration());
        clone.setActions(original.getActions());
        clone.setCategory(original.getCategory());
        clone.setStartDate(req != null && req.getStartDate() != null ? req.getStartDate() : original.getStartDate());
        clone.setEndDate(original.getEndDate());
        clone.setIsPublic(false); // Private copy
        clone.setSourceId(original.getId()); // Store origin
        clone.setIsSubscribed(true);
        
        if (req != null && Boolean.TRUE.equals(req.getActivate())) {
            archiveActivePlans(userId);
            clone.setStatus("ACTIVE");
        } else {
            clone.setStatus("PLANNING"); // Reset status
        }
        
        trainingMapper.insert(clone);
        return Result.success();
    }
}
