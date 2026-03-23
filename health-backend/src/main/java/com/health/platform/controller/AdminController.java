package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.*;
import com.health.platform.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administrator Management")
@RestController
@RequestMapping("/admin")
@SaCheckRole("ADMIN")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private ExerciseMapper exerciseMapper;

    // --- User Management ---
    @Operation(summary = "Get all users")
    @GetMapping("/users")
    public Result<List<SysUser>> getUsers() {
        return Result.success(userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreateTime)));
    }

    @Operation(summary = "Update user role/status")
    @PostMapping("/user/update")
    public Result<Void> updateUser(@RequestBody SysUser user) {
        if (user.getId() == null) return Result.error("User ID required");
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/user/{id}")
    public Result<Void> deleteUser(@PathVariable Integer id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    // --- Content (Post) Management ---
    @Operation(summary = "Get all community posts")
    @GetMapping("/posts")
    public Result<List<CommunityPost>> getPosts() {
        return Result.success(postMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                .orderByDesc(CommunityPost::getCreateTime)));
    }

    @Operation(summary = "Delete community post")
    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Integer id) {
        postMapper.deleteById(id);
        return Result.success();
    }

    // --- Training Library Management (Public) ---
    @Operation(summary = "Get all library plans")
    @GetMapping("/plans")
    public Result<List<TrainingPlan>> getLibraryPlans() {
        return Result.success(trainingMapper.selectList(new LambdaQueryWrapper<TrainingPlan>()
                .eq(TrainingPlan::getIsPublic, true)));
    }

    @Operation(summary = "Create or Update a library plan")
    @PostMapping("/plan/save")
    public Result<Void> saveLibraryPlan(@RequestBody TrainingPlan plan) {
        plan.setIsPublic(true); // Always public when managed here
        if (plan.getId() == null) {
            trainingMapper.insert(plan);
        } else {
            trainingMapper.updateById(plan);
        }
        return Result.success();
    }

    @Operation(summary = "Delete library plan")
    @DeleteMapping("/plan/{id}")
    public Result<Void> deleteLibraryPlan(@PathVariable Integer id) {
        trainingMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "Get all library courses")
    @GetMapping("/courses")
    public Result<List<Course>> getLibraryCourses() {
        return Result.success(courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublic, true)));
    }

    @Operation(summary = "Create or Update a library course")
    @PostMapping("/course/save")
    public Result<Void> saveLibraryCourse(@RequestBody Course course) {
        course.setIsPublic(true); // Always public when managed here
        if (course.getId() == null) {
            courseMapper.insert(course);
        } else {
            courseMapper.updateById(course);
        }
        return Result.success();
    }

    @Operation(summary = "Delete library course")
    @DeleteMapping("/course/{id}")
    public Result<Void> deleteLibraryCourse(@PathVariable Integer id) {
        courseMapper.deleteById(id);
        return Result.success();
    }

    // --- Comment Management ---
    @Autowired
    private CommentMapper commentMapper;

    @Operation(summary = "Get all comments")
    @GetMapping("/comments")
    public Result<List<HealthComment>> getComments() {
        return Result.success(commentMapper.selectList(new LambdaQueryWrapper<HealthComment>()
                .orderByDesc(HealthComment::getCreateTime)));
    }

    @Operation(summary = "Delete comment")
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Integer id) {
        commentMapper.deleteById(id);
        return Result.success();
    }
}
