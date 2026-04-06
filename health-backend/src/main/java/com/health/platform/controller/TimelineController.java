package com.health.platform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.common.Result;
import com.health.platform.entity.*;
import com.health.platform.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/timeline")
@SaCheckLogin
public class TimelineController {

    @Autowired
    private ActivityParticipationMapper participationMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TrainingRecordMapper trainingRecordMapper;

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private DailyScheduleMapper dailyScheduleMapper;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "Get user timeline activities")
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> getMyTimeline(
            @RequestParam(value = "type", defaultValue = "all") String type
    ) {
        Integer userId = StpUtil.getLoginIdAsInt();
        List<Map<String, Object>> timeline = new ArrayList<>();

        // 1. Activity participations
        if ("all".equals(type) || "community".equals(type)) {
            List<ActivityParticipation> participations = participationMapper.selectList(
                    new LambdaQueryWrapper<ActivityParticipation>()
                            .eq(ActivityParticipation::getUserId, userId)
                            .orderByDesc(ActivityParticipation::getApplyTime)
            );

            for (ActivityParticipation part : participations) {
                Activity activity = activityMapper.selectById(part.getActivityId());
                if (activity != null) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", "act_" + part.getId());
                    item.put("type", "activity");
                    item.put("timestamp", part.getApplyTime());
                    item.put("activityId", activity.getId());
                    item.put("title", activity.getTitle());
                    item.put("activityType", activity.getActivityType());
                    item.put("status", part.getStatus());
                    item.put("applyTime", part.getApplyTime());
                    
                    // 获取活动类型描述
                    String actTypeDesc = getActivityTypeDesc(activity.getActivityType());
                    item.put("description", "参与了" + actTypeDesc + "活动：" + activity.getTitle());
                    
                    timeline.add(item);
                }
            }

            // 2. Community posts
            List<CommunityPost> posts = postMapper.selectList(
                    new LambdaQueryWrapper<CommunityPost>()
                            .eq(CommunityPost::getUserId, userId)
                            .eq(CommunityPost::getStatus, "published")
                            .orderByDesc(CommunityPost::getCreateTime)
            );

            for (CommunityPost post : posts) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", "post_" + post.getId());
                item.put("type", "post");
                item.put("timestamp", post.getCreateTime());
                item.put("postId", post.getId());
                item.put("title", post.getTitle());
                item.put("content", post.getContent());
                item.put("images", post.getImages());
                item.put("likeCount", post.getLikeCount());
                item.put("commentCount", post.getCommentCount());
                item.put("description", "发布了帖子：" + post.getTitle());
                
                timeline.add(item);
            }
        }

        // 3. Training records
        if ("all".equals(type) || "training".equals(type)) {
            List<TrainingRecord> records = trainingRecordMapper.selectList(
                    new LambdaQueryWrapper<TrainingRecord>()
                            .eq(TrainingRecord::getUserId, userId)
                            .orderByDesc(TrainingRecord::getRecordTime)
            );

            for (TrainingRecord record : records) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", "train_" + record.getId());
                item.put("type", "training");
                item.put("timestamp", record.getRecordTime());
                item.put("recordId", record.getId());
                item.put("duration", record.getCompleteDuration());
                item.put("difficulty", record.getDifficulty());
                item.put("feeling", record.getFeeling());
                item.put("sourceType", record.getSourceType());
                item.put("sourceId", record.getSourceId());

                // 获取训练名称
                String trainingName = getTrainingName(record);
                item.put("title", trainingName);
                item.put("description", "完成了" + record.getCompleteDuration() + "分钟的" + trainingName + "训练");

                timeline.add(item);
            }
        }

        // Sort by timestamp descending
        timeline.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeB.compareTo(timeA);
        });

        return Result.success(timeline);
    }

    private String getActivityTypeDesc(Integer activityType) {
        if (activityType == null) return "普通";
        switch (activityType) {
            case 1: return "打卡";
            case 2: return "挑战";
            case 3: return "话题";
            default: return "普通";
        }
    }

    private String getTrainingName(TrainingRecord record) {
        if ("COURSE".equals(record.getSourceType())) {
            Course course = courseMapper.selectById(record.getSourceId());
            return course != null ? course.getTitle() : "课程训练";
        } else if ("SCHEDULE".equals(record.getSourceType())) {
            DailySchedule schedule = dailyScheduleMapper.selectById(record.getSourceId());
            if (schedule != null) {
                // DailySchedule 有 planId 和 courseId 字段
                if (schedule.getPlanId() != null) {
                    TrainingPlan plan = trainingMapper.selectById(schedule.getPlanId());
                    return plan != null ? plan.getTitle() : "计划训练";
                } else if (schedule.getCourseId() != null) {
                    Course course = courseMapper.selectById(schedule.getCourseId());
                    return course != null ? course.getTitle() : "课程训练";
                }
                return schedule.getTitle() != null ? schedule.getTitle() : "日常训练";
            }
        }
        return "系统训练";
    }
}
