package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.platform.common.Result;
import com.health.platform.entity.*;
import com.health.platform.mapper.*;
import com.health.platform.service.ActivityProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Tag(name = "Community")
@RestController
@RequestMapping("/community")
@SaCheckLogin
public class CommunityController {

    private static final Pattern HASHTAG = Pattern.compile("#([^\\s#]+)");

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private StarMapper starMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CollectionMapper collectionMapper;
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private ActivityParticipationMapper participationMapper;
    
    @Autowired
    private ActivityTaskMapper taskMapper;

    @Autowired
    private ActivityProgressService activityProgressService;

    // ─── Posts ───────────────────────────────────────────

    @Operation(summary = "Get all posts (feed)")
    @GetMapping("/posts")
    public Result<List<CommunityPost>> getPosts(
            @RequestParam(value = "tab", defaultValue = "recommend") String tab
    ) {
        Integer userId = StpUtil.getLoginIdAsInt();
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getStatus, "published");

        if ("mine".equals(tab)) {
            wrapper.eq(CommunityPost::getUserId, userId);
        } else if ("following".equals(tab)) {
            // Find who I follow
            List<UserFollow> follows = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                    .eq(UserFollow::getFollowerId, userId));
            List<Integer> followeeIds = follows.stream().map(UserFollow::getFolloweeId).toList();
            
            if (followeeIds.isEmpty()) {
                return Result.success(new java.util.ArrayList<>());
            }
            
            // Following tab should not show my own posts, and only show followed users' posts
            wrapper.in(CommunityPost::getUserId, followeeIds)
                   .ne(CommunityPost::getUserId, userId);
        }

        if ("hot".equals(tab)) {
            wrapper.orderByDesc(CommunityPost::getLikeCount);
        } else {
            wrapper.orderByDesc(CommunityPost::getCreateTime);
        }

        List<CommunityPost> list = postMapper.selectList(wrapper);
        fillPostDetails(list, userId);
        return Result.success(list);
    }
    
    private void fillPostDetails(List<CommunityPost> list, Integer currentUserId) {
        for (CommunityPost post : list) {
            // Fill nickname
            SysUser user = userMapper.selectById(post.getUserId());
            if (user != null) {
                post.setNickname(user.getNickname());
            } else {
                post.setNickname("用户 #" + post.getUserId());
            }
            
            // Interaction status
            if (currentUserId != null) {
                post.setIsLiked(starMapper.selectCount(new LambdaQueryWrapper<HealthStar>()
                    .eq(HealthStar::getUserId, currentUserId)
                    .eq(HealthStar::getTargetId, post.getId())
                    .eq(HealthStar::getTargetType, "POST")) > 0);
                
                post.setIsCollected(collectionMapper.selectCount(new LambdaQueryWrapper<Collection>()
                    .eq(Collection::getUserId, currentUserId)
                    .eq(Collection::getTargetId, post.getId())
                    .eq(Collection::getTargetType, "POST")) > 0);
            }
        }
    }

    @Operation(summary = "Search posts (unified search)")
    @GetMapping("/search")
    public Result<List<CommunityPost>> search(@RequestParam("q") String q) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getStatus, "published")
                .and(w -> w.like(CommunityPost::getTitle, q)
                        .or().like(CommunityPost::getContent, q)
                        .or().like(CommunityPost::getTags, q));

        wrapper.orderByDesc(CommunityPost::getCreateTime);
        List<CommunityPost> list = postMapper.selectList(wrapper);
        fillPostDetails(list, StpUtil.getLoginIdAsInt());
        return Result.success(list);
    }

    @Operation(summary = "Create a community post")
    @PostMapping("/post")
    @org.springframework.transaction.annotation.Transactional
    public Result<Void> createPost(@RequestBody CommunityPost post) {
        Integer userId = StpUtil.getLoginIdAsInt();
        post.setUserId(userId);
        post.setStatus("published");
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);
        post.setCreateTime(java.time.LocalDateTime.now());

        Set<String> tagSet = new LinkedHashSet<>();
        collectHashtags(post.getTitle(), tagSet);
        collectHashtags(post.getContent(), tagSet);
        if (!tagSet.isEmpty()) {
            post.setTags(String.join(",", tagSet));
        }

        postMapper.insert(post);

        // 更新话题类活动的参与状态（依赖 post.tags）
        updateTopicActivityProgress(userId, post.getTags());
        
        return Result.success();
    }
    
    private static void collectHashtags(String text, Set<String> out) {
        if (text == null || text.isEmpty()) return;
        Matcher m = HASHTAG.matcher(text);
        while (m.find()) {
            String raw = m.group(1).trim();
            if (!raw.isEmpty()) out.add(raw);
        }
    }

    private void updateTopicActivityProgress(Integer userId, String tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        List<Activity> topicActivities = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getActivityType, 3)
                        .eq(Activity::getStatus, "ONLINE"));

        String[] tagArray = tags.split(",");
        for (String rawTag : tagArray) {
            String tag = rawTag.trim();
            if (tag.isEmpty()) continue;

            for (Activity activity : topicActivities) {
                String tn = activity.getTopicName() != null ? activity.getTopicName().replace("#", "").trim() : "";
                if (tn.isEmpty() || !tn.equalsIgnoreCase(tag)) {
                    continue;
                }
                ActivityParticipation part = participationMapper.selectOne(
                        new LambdaQueryWrapper<ActivityParticipation>()
                                .eq(ActivityParticipation::getActivityId, activity.getId())
                                .eq(ActivityParticipation::getUserId, userId));
                if (part != null) {
                    activityProgressService.syncParticipationStatus(part.getId());
                }
            }
        }
    }

    @Operation(summary = "Delete own post")
    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable("id") Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        postMapper.delete(new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getId, id)
                .eq(CommunityPost::getUserId, userId));
        return Result.success();
    }

    // ─── Comments on posts ───────────────────────────────

    @Operation(summary = "Get comments for a post")
    @GetMapping("/post/{id}/comments")
    public Result<List<HealthComment>> getComments(@PathVariable("id") Integer id) {
        List<HealthComment> list = commentMapper.selectList(new LambdaQueryWrapper<HealthComment>()
                .eq(HealthComment::getTargetId, id)
                .eq(HealthComment::getTargetType, "POST")
                .orderByDesc(HealthComment::getCreateTime));
        
        for (HealthComment c : list) {
            SysUser user = userMapper.selectById(c.getUserId());
            if (user != null) {
                c.setNickname(user.getNickname());
            } else {
                c.setNickname("用户 #" + c.getUserId());
            }
        }
        return Result.success(list);
    }

    @Operation(summary = "Add a comment to a post")
    @PostMapping("/post/{id}/comment")
    public Result<Void> addComment(@PathVariable("id") Integer id, @RequestBody HealthComment comment) {
        comment.setUserId(StpUtil.getLoginIdAsInt());
        comment.setTargetId(id);
        comment.setTargetType("POST");
        commentMapper.insert(comment);
        // Increment comment count
        CommunityPost post = postMapper.selectById(id);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }
        return Result.success();
    }

    // ─── Likes ───────────────────────────────────────────

    @Operation(summary = "Like a post")
    @PostMapping("/post/{id}/like")
    public Result<Void> like(@PathVariable("id") Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        HealthStar star = new HealthStar();
        star.setUserId(userId);
        star.setTargetId(id);
        star.setTargetType("POST");
        try {
            starMapper.insert(star);
            CommunityPost post = postMapper.selectById(id);
            if (post != null) {
                post.setLikeCount((post.getLikeCount() == null ? 0 : post.getLikeCount()) + 1);
                postMapper.updateById(post);
            }
        } catch (Exception ignored) {} 
        return Result.success();
    }

    @Operation(summary = "Unlike a post")
    @DeleteMapping("/post/{id}/like")
    public Result<Void> unlike(@PathVariable("id") Integer id) {
        Integer userId = StpUtil.getLoginIdAsInt();
        int deleted = starMapper.delete(new LambdaQueryWrapper<HealthStar>()
                .eq(HealthStar::getUserId, userId)
                .eq(HealthStar::getTargetId, id)
                .eq(HealthStar::getTargetType, "POST"));
        
        if (deleted > 0) {
            CommunityPost post = postMapper.selectById(id);
            if (post != null) {
                post.setLikeCount(Math.max(0, (post.getLikeCount() == null ? 0 : post.getLikeCount()) - 1));
                postMapper.updateById(post);
            }
        }
        return Result.success();
    }

    // ─── Follow ──────────────────────────────────────────

    @Operation(summary = "Follow a user")
    @PostMapping("/follow/{userId}")
    public Result<Void> follow(@PathVariable("userId") Integer userId) {
        Integer currentUserId = StpUtil.getLoginIdAsInt();
        if (currentUserId.equals(userId)) {
            return Result.error("不能关注自己");
        }
        UserFollow follow = new UserFollow();
        follow.setFollowerId(currentUserId);
        follow.setFolloweeId(userId);
        try {
            userFollowMapper.insert(follow);
        } catch (Exception ignored) {}
        return Result.success();
    }

    @Operation(summary = "Unfollow a user")
    @DeleteMapping("/follow/{userId}")
    public Result<Void> unfollow(@PathVariable("userId") Integer userId) {
        userFollowMapper.delete(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, StpUtil.getLoginIdAsInt())
                .eq(UserFollow::getFolloweeId, userId));
        return Result.success();
    }

    @Operation(summary = "Get hot posts for right sidebar")
    @GetMapping("/hot")
    public Result<List<CommunityPost>> getHotPosts() {
        List<CommunityPost> list = postMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getStatus, "published")
                .orderByDesc(CommunityPost::getLikeCount)
                .last("LIMIT 5"));
        fillPostDetails(list, StpUtil.getLoginIdAsInt());
        return Result.success(list);
    }

    @Operation(summary = "Get basic user info and follow status")
    @GetMapping("/user/{id}")
    public Result<java.util.Map<String, Object>> getUserInfo(@PathVariable("id") Integer id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) return Result.error("用户不存在");
        
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", user.getId());
        map.put("nickname", user.getNickname());
        map.put("isFollowing", userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, StpUtil.getLoginIdAsInt())
                .eq(UserFollow::getFolloweeId, id)) > 0);
        
        // Count followers and following
        map.put("followerCount", userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFolloweeId, id)));
        map.put("followingCount", userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, id)));
        
        return Result.success(map);
    }
}
