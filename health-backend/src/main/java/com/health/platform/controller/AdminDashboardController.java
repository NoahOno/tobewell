package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.health.platform.common.Result;
import com.health.platform.mapper.*;
import com.health.platform.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Admin Dashboard")
@RestController
@RequestMapping("/admin/dashboard")
@SaCheckRole("ADMIN")
public class AdminDashboardController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TrainingMapper trainingMapper;
    @Autowired
    private ActivityMapper activityMapper;

    @Operation(summary = "Get admin dashboard statistics")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // --- Total counts ---
        // Normal users + Banned users (not deactivated)
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .ne(SysUser::getStatus, 2)); 
        Long postCount = postMapper.selectCount(null);
        Long courseCount = courseMapper.selectCount(null);
        Long planCount = trainingMapper.selectCount(null);
        Long activityCount = activityMapper.selectCount(null);

        stats.put("totalUsers", userCount);
        stats.put("totalPosts", postCount);
        stats.put("totalCourses", courseCount);
        stats.put("totalPlans", planCount);
        stats.put("totalActivities", activityCount);

        // --- Status breakdown (Example for UI/UX) ---
        stats.put("bannedUsers", userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 0)));
        stats.put("activeUsers", userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1)));
        
        return Result.success(stats);
    }
}
