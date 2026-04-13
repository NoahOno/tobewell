package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.*;
import com.health.platform.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interaction")
@SaCheckLogin
public class InteractionController {

    @Autowired
    private StarMapper starMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private SubscriptionMapper subscriptionMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    // --- Stars (Likes) ---
    @PostMapping("/star")
    public Result<Void> star(@RequestBody HealthStar star) {
        star.setUserId(StpUtil.getLoginIdAsInt());
        try {
            starMapper.insert(star);
            if ("POST".equals(star.getTargetType())) {
                CommunityPost post = postMapper.selectById(star.getTargetId());
                if (post != null) {
                    post.setLikeCount((post.getLikeCount() == null ? 0 : post.getLikeCount()) + 1);
                    postMapper.updateById(post);
                }
            }
        } catch (Exception ignored) {}
        return Result.success();
    }

    @DeleteMapping("/star")
    public Result<Void> unstar(@RequestParam("targetId") Integer targetId, @RequestParam("targetType") String targetType) {
        Integer userId = StpUtil.getLoginIdAsInt();
        int deleted = starMapper.delete(new LambdaQueryWrapper<HealthStar>()
                .eq(HealthStar::getUserId, userId)
                .eq(HealthStar::getTargetId, targetId)
                .eq(HealthStar::getTargetType, targetType));
        
        if (deleted > 0 && "POST".equals(targetType)) {
            CommunityPost post = postMapper.selectById(targetId);
            if (post != null) {
                post.setLikeCount(Math.max(0, (post.getLikeCount() == null ? 0 : post.getLikeCount()) - 1));
                postMapper.updateById(post);
            }
        }
        return Result.success();
    }

    // --- Comments ---
    @PostMapping("/comment")
    public Result<Void> comment(@RequestBody HealthComment comment) {
        comment.setUserId(StpUtil.getLoginIdAsInt());
        commentMapper.insert(comment);
        if ("POST".equals(comment.getTargetType())) {
            CommunityPost post = postMapper.selectById(comment.getTargetId());
            if (post != null) {
                post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
                postMapper.updateById(post);
            }
        }
        return Result.success();
    }

    @GetMapping("/comments")
    public Result<List<HealthComment>> getComments(@RequestParam("targetId") Integer targetId, @RequestParam("targetType") String targetType) {
        List<HealthComment> list = commentMapper.selectList(new LambdaQueryWrapper<HealthComment>()
                .eq(HealthComment::getTargetId, targetId)
                .eq(HealthComment::getTargetType, targetType)
                .orderByDesc(HealthComment::getCreateTime));
        return Result.success(list);
    }

    // --- Collections ---
    @PostMapping("/collect")
    public Result<Void> collect(@RequestBody Collection collection) {
        collection.setUserId(StpUtil.getLoginIdAsInt());
        try {
            // Auto-fill title for POST type if not provided
            if ("POST".equals(collection.getTargetType()) && collection.getTargetTitle() == null) {
                CommunityPost post = postMapper.selectById(collection.getTargetId());
                if (post != null) {
                    collection.setTargetTitle(post.getTitle());
                }
            }
            collectionMapper.insert(collection);
            if ("POST".equals(collection.getTargetType())) {
                CommunityPost post = postMapper.selectById(collection.getTargetId());
                if (post != null) {
                    post.setCollectionCount((post.getCollectionCount() == null ? 0 : post.getCollectionCount()) + 1);
                    postMapper.updateById(post);
                }
            }
        } catch (Exception ignored) {}
        return Result.success();
    }

    @DeleteMapping("/collect")
    public Result<Void> uncollect(@RequestParam("targetId") Integer targetId, @RequestParam("targetType") String targetType) {
        Integer userId = StpUtil.getLoginIdAsInt();
        int deleted = collectionMapper.delete(new LambdaQueryWrapper<Collection>()
                .eq(Collection::getUserId, userId)
                .eq(Collection::getTargetId, targetId)
                .eq(Collection::getTargetType, targetType));
        
        if (deleted > 0 && "POST".equals(targetType)) {
            CommunityPost post = postMapper.selectById(targetId);
            if (post != null) {
                post.setCollectionCount(Math.max(0, (post.getCollectionCount() == null ? 0 : post.getCollectionCount()) - 1));
                postMapper.updateById(post);
            }
        }
        return Result.success();
    }

    // --- Subscriptions ---
    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestBody Subscription subscription) {
        subscription.setUserId(StpUtil.getLoginIdAsInt());
        subscriptionMapper.insert(subscription);
        return Result.success();
    }

    @GetMapping("/collections")
    public Result<List<Map<String, Object>>> getCollections(@RequestParam("type") String type) {
        Integer userId = StpUtil.getLoginIdAsInt();
        List<Collection> list = collectionMapper.selectList(new LambdaQueryWrapper<Collection>()
                .eq(Collection::getUserId, userId)
                .eq(Collection::getTargetType, type)
                .orderByDesc(Collection::getCreateTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Collection c : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("targetId", c.getTargetId());
            map.put("targetType", c.getTargetType());
            map.put("targetTitle", c.getTargetTitle());
            map.put("createTime", c.getCreateTime());

            if ("POST".equals(type)) {
                CommunityPost post = postMapper.selectById(c.getTargetId());
                if (post != null) {
                    map.put("targetTitle", post.getTitle());
                    map.put("content", (post.getContent() != null && post.getContent().length() > 100)
                            ? post.getContent().substring(0, 100) + "..." : post.getContent());
                    map.put("likeCount", post.getLikeCount());
                    map.put("commentCount", post.getCommentCount());
                    map.put("images", post.getImages());
                    SysUser author = userMapper.selectById(post.getUserId());
                    map.put("authorName", author != null ? author.getNickname() : "未知用户");
                    map.put("authorAvatar", author != null ? author.getAvatar() : null);
                    map.put("postUserId", post.getUserId());
                }
            }
            result.add(map);
        }
        return Result.success(result);
    }
}
