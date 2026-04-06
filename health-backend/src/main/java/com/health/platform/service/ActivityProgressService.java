package com.health.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.entity.Activity;
import com.health.platform.entity.ActivityParticipation;
import com.health.platform.entity.ActivityTask;
import com.health.platform.entity.CommunityPost;
import com.health.platform.entity.DailySchedule;
import com.health.platform.entity.TrainingPlan;
import com.health.platform.entity.TrainingRecord;
import com.health.platform.mapper.ActivityMapper;
import com.health.platform.mapper.TrainingMapper;
import com.health.platform.mapper.ActivityParticipationMapper;
import com.health.platform.mapper.ActivityTaskMapper;
import com.health.platform.mapper.DailyScheduleMapper;
import com.health.platform.mapper.PostMapper;
import com.health.platform.mapper.TrainingRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActivityProgressService {

    public static final String POST_STATUS_PUBLISHED = "published";

    @Autowired
    private ActivityTaskMapper taskMapper;
    @Autowired
    private TrainingRecordMapper trainingRecordMapper;
    @Autowired
    private DailyScheduleMapper dailyScheduleMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityParticipationMapper participationMapper;

    @Autowired
    private TrainingMapper trainingMapper;

    /**
     * 挑战活动 templateId 指向库里的「模板计划」；用户参与后日程上的 plan_id 可能是为其克隆的个人计划 id。
     */
    private Integer effectivePlanIdForChallenge(Integer userId, Integer templatePlanId) {
        if (templatePlanId == null) {
            return null;
        }
        TrainingPlan template = trainingMapper.selectById(templatePlanId);
        if (template != null && userId.equals(template.getUserId())) {
            return templatePlanId;
        }
        TrainingPlan copy = trainingMapper.selectOne(new LambdaQueryWrapper<TrainingPlan>()
                .eq(TrainingPlan::getUserId, userId)
                .eq(TrainingPlan::getSourceId, templatePlanId)
                .last("LIMIT 1"));
        return copy != null ? copy.getId() : templatePlanId;
    }

    public static boolean tagsContainTopic(String tagsCsv, String topic) {
        if (tagsCsv == null || topic == null || topic.isEmpty()) return false;
        for (String t : tagsCsv.split(",")) {
            if (topic.equalsIgnoreCase(t.trim())) return true;
        }
        return false;
    }

    /**
     * Training records for schedules store {@link DailySchedule#getId()} in {@link TrainingRecord#getSourceId()},
     * not the plan id. Filter records whose schedule belongs to the given plan.
     */
    private List<TrainingRecord> recordsForPlanSchedule(Integer userId, Integer planId, LocalDateTime from, LocalDateTime to) {
        List<TrainingRecord> records = trainingRecordMapper.selectList(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getSourceType, "SCHEDULE")
                .ge(TrainingRecord::getRecordTime, from)
                .le(TrainingRecord::getRecordTime, to));
        return records.stream()
                .filter(r -> {
                    if (r.getSourceId() == null) return false;
                    DailySchedule ds = dailyScheduleMapper.selectById(r.getSourceId());
                    return ds != null && ds.getPlanId() != null && ds.getPlanId().equals(planId);
                })
                .collect(Collectors.toList());
    }

    private List<TrainingRecord> recordsForPlanSchedule(Integer userId, Integer planId, LocalDateTime from) {
        List<TrainingRecord> records = trainingRecordMapper.selectList(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getUserId, userId)
                .eq(TrainingRecord::getSourceType, "SCHEDULE")
                .ge(TrainingRecord::getRecordTime, from));
        return records.stream()
                .filter(r -> {
                    if (r.getSourceId() == null) return false;
                    DailySchedule ds = dailyScheduleMapper.selectById(r.getSourceId());
                    return ds != null && ds.getPlanId() != null && ds.getPlanId().equals(planId);
                })
                .collect(Collectors.toList());
    }

    public long[] computeProgress(Integer userId, Activity activity, ActivityParticipation part) {
        long completed = 0;
        long total = activity.getRequiredDays() != null ? activity.getRequiredDays() : 7;

        if (activity.getActivityType() == 1) {
            String countMode = activity.getCountMode() != null ? activity.getCountMode() : "DAYS";
            List<ActivityTask> done = taskMapper.selectList(new LambdaQueryWrapper<ActivityTask>()
                    .eq(ActivityTask::getParticipationId, part.getId())
                    .eq(ActivityTask::getStatus, "COMPLETED"));
            if ("COUNT".equals(countMode)) {
                completed = done.size();
            } else {
                completed = done.stream()
                        .map(ActivityTask::getTaskDate)
                        .filter(d -> d != null)
                        .distinct()
                        .count();
            }
        } else if (activity.getActivityType() == 2) {
            if (activity.getTemplateId() != null) {
                LocalDateTime applyTime = part.getApplyTime();
                LocalDate startDate = applyTime.toLocalDate();
                String countMode = activity.getCountMode() != null ? activity.getCountMode() : "DAYS";

                if ("COUNT".equals(countMode)) {
                    if ("PLAN".equals(activity.getTemplateType())) {
                        Integer pid = effectivePlanIdForChallenge(userId, activity.getTemplateId());
                        completed = pid != null ? recordsForPlanSchedule(userId, pid, applyTime).size() : 0;
                    } else if ("COURSE".equals(activity.getTemplateType())) {
                        completed = trainingRecordMapper.selectCount(new LambdaQueryWrapper<TrainingRecord>()
                                .eq(TrainingRecord::getUserId, userId)
                                .eq(TrainingRecord::getSourceType, "COURSE")
                                .eq(TrainingRecord::getSourceId, activity.getTemplateId())
                                .ge(TrainingRecord::getRecordTime, applyTime));
                    }
                } else {
                    LocalDate endDate = startDate.plusDays((int) total - 1);
                    LocalDateTime endDt = endDate.atTime(23, 59, 59);
                    if ("PLAN".equals(activity.getTemplateType())) {
                        Integer pid = effectivePlanIdForChallenge(userId, activity.getTemplateId());
                        List<TrainingRecord> records = pid != null
                                ? recordsForPlanSchedule(userId, pid, startDate.atStartOfDay(), endDt)
                                : List.of();
                        Set<LocalDate> uniqueDays = new HashSet<>();
                        for (TrainingRecord r : records) {
                            uniqueDays.add(r.getRecordTime().toLocalDate());
                        }
                        completed = uniqueDays.size();
                    } else if ("COURSE".equals(activity.getTemplateType())) {
                        List<TrainingRecord> records = trainingRecordMapper.selectList(new LambdaQueryWrapper<TrainingRecord>()
                                .eq(TrainingRecord::getUserId, userId)
                                .eq(TrainingRecord::getSourceType, "COURSE")
                                .eq(TrainingRecord::getSourceId, activity.getTemplateId())
                                .ge(TrainingRecord::getRecordTime, startDate.atStartOfDay())
                                .le(TrainingRecord::getRecordTime, endDt));
                        Set<LocalDate> uniqueDays = new HashSet<>();
                        for (TrainingRecord r : records) {
                            uniqueDays.add(r.getRecordTime().toLocalDate());
                        }
                        completed = uniqueDays.size();
                    }
                }
            }
        } else if (activity.getActivityType() == 3 && activity.getTopicName() != null) {
            String topic = activity.getTopicName().replace("#", "").trim();
            String statMode = activity.getTopicStatMode() != null ? activity.getTopicStatMode() : "COUNT";

            List<CommunityPost> posts = postMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                    .eq(CommunityPost::getUserId, userId)
                    .ge(CommunityPost::getCreateTime, part.getApplyTime())
                    .eq(CommunityPost::getStatus, POST_STATUS_PUBLISHED));

            List<CommunityPost> topicPosts = posts.stream()
                    .filter(p -> tagsContainTopic(p.getTags(), topic))
                    .collect(Collectors.toList());

            if ("SHARED".equals(statMode)) {
                completed = topicPosts.isEmpty() ? 0 : 1;
                total = 1;
            } else if ("DAYS".equals(statMode)) {
                Set<LocalDate> uniqueDays = new HashSet<>();
                for (CommunityPost post : topicPosts) {
                    uniqueDays.add(post.getCreateTime().toLocalDate());
                }
                completed = uniqueDays.size();
            } else {
                completed = topicPosts.size();
            }
        }

        return new long[]{completed, total};
    }

    public void syncParticipationStatus(Integer participationId) {
        ActivityParticipation part = participationMapper.selectById(participationId);
        if (part == null) return;
        Activity activity = activityMapper.selectById(part.getActivityId());
        if (activity == null) return;
        long[] p = computeProgress(part.getUserId(), activity, part);
        if (p[1] > 0 && p[0] >= p[1]) {
            part.setStatus("COMPLETED");
        } else if (p[0] > 0) {
            part.setStatus("IN_PROGRESS");
        } else {
            part.setStatus("APPLIED");
        }
        participationMapper.updateById(part);
    }

    /** After any training record change, refresh all joined online challenge activities for this user. */
    public void refreshChallengeParticipationsForUser(Integer userId) {
        List<ActivityParticipation> parts = participationMapper.selectList(
                new LambdaQueryWrapper<ActivityParticipation>().eq(ActivityParticipation::getUserId, userId));
        for (ActivityParticipation part : parts) {
            Activity act = activityMapper.selectById(part.getActivityId());
            if (act == null || act.getActivityType() != 2 || !"ONLINE".equals(act.getStatus())) continue;
            syncParticipationStatus(part.getId());
        }
    }
}
