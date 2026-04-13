package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.SysUser;
import com.health.platform.mapper.CollectionMapper;
import com.health.platform.mapper.PostMapper;
import com.health.platform.mapper.UserFollowMapper;
import com.health.platform.mapper.UserMapper;
import com.health.platform.entity.CommunityPost;
import com.health.platform.entity.UserFollow;
import com.health.platform.entity.Collection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserFollowMapper followMapper;

    @Autowired
    private CollectionMapper collectionMapper;

    @Operation(summary = "Register")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody SysUser user) {
        SysUser existing = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername()));
        if (existing != null) {
            return Result.error("Username already exists");
        }
        user.setRole("USER");
        userMapper.insert(user);
        return Result.success();
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public Result<String> login(@RequestBody SysUser loginUser) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginUser.getUsername()));
        
        if (user == null || !user.getPassword().equals(loginUser.getPassword())) {
            return Result.error("Invalid username or password");
        }

        StpUtil.login(user.getId());
        return Result.success(StpUtil.getTokenValue());
    }

    @Operation(summary = "Get current user info")
    @GetMapping("/info")
    public Result<SysUser> getInfo() {
        Integer userId = StpUtil.getLoginIdAsInt();
        SysUser user = userMapper.selectById(userId);
        user.setPassword(null); // Don't return password
        return Result.success(user);
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    @Operation(summary = "Update profile")
    @SaCheckLogin
    @PostMapping("/update")
    public Result<Void> update(@RequestBody SysUser user) {
        Integer userId = StpUtil.getLoginIdAsInt();
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, userId);

        if (user.getNickname() != null) {
            updateWrapper.set(SysUser::getNickname, user.getNickname());
        }
        if (user.getPassword() != null) {
            updateWrapper.set(SysUser::getPassword, user.getPassword());
        }
        if (user.getAvatar() != null) {
            updateWrapper.set(SysUser::getAvatar, user.getAvatar());
        }

        userMapper.update(null, updateWrapper);
        return Result.success();
    }

    @Operation(summary = "Get recommended users")
    @GetMapping("/recommendations")
    public Result<List<SysUser>> getRecommendations() {
        return Result.success(userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .ne(SysUser::getId, StpUtil.getLoginIdAsInt())
                .last("LIMIT 3")));
    }

    @Operation(summary = "Get user statistics")
    @GetMapping("/stats")
    @SaCheckLogin
    public Result<Map<String, Object>> getStats() {
        Integer userId = StpUtil.getLoginIdAsInt();
        Map<String, Object> stats = new HashMap<>();
        
        // Post count
        Long postCount = postMapper.selectCount(new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getUserId, userId));
        
        // Following count
        Long followingCount = followMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId));
        
        // Followers count
        Long followersCount = followMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFolloweeId, userId));
        
        // Collection count
        Long collectionCount = collectionMapper.selectCount(new LambdaQueryWrapper<Collection>()
                .eq(Collection::getUserId, userId));
        
        stats.put("postCount", postCount);
        stats.put("followingCount", followingCount);
        stats.put("followersCount", followersCount);
        stats.put("collectionCount", collectionCount);
        
        return Result.success(stats);
    }
}
