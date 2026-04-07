package com.health.platform.service.impl;

import com.health.platform.entity.DailySchedule;
import com.health.platform.entity.TrainingPlan;
import com.health.platform.mapper.DailyScheduleMapper;
import com.health.platform.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private DailyScheduleMapper scheduleMapper;

    @Autowired
    private com.health.platform.mapper.CourseMapper courseMapper;

    @Override
    public void generateSchedule(TrainingPlan userPlan, List<String> weeklyDays) {
        if (weeklyDays == null || weeklyDays.isEmpty()) {
            weeklyDays = List.of("MONDAY", "WEDNESDAY", "FRIDAY"); // Default
        }
        
        System.out.println("[ScheduleService] Generating schedule for plan: " + userPlan.getId() + ", userId: " + userPlan.getUserId());
        System.out.println("[ScheduleService] Start date: " + userPlan.getStartDate() + ", weeklyDays: " + weeklyDays);
        System.out.println("[ScheduleService] Actions JSON: " + userPlan.getActions());
        
        // Clear existing future schedules to allow regenerating
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DailySchedule> deleteWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        deleteWrapper.eq("plan_id", userPlan.getId());
        int deleted = scheduleMapper.delete(deleteWrapper);
        System.out.println("[ScheduleService] Deleted " + deleted + " old schedules");
        
        // Parse the plan actions JSON
        List<Map<String, Object>> planDays;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            planDays = mapper.readValue(userPlan.getActions(), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            System.err.println("[ScheduleService] Failed to parse actions JSON: " + e.getMessage());
            return; // Invalid plan structure
        }

        if (planDays.isEmpty()) {
            System.out.println("[ScheduleService] No plan days found");
            return;
        }
        
        System.out.println("[ScheduleService] Found " + planDays.size() + " plan days");

        LocalDate current = userPlan.getStartDate() != null ? userPlan.getStartDate() : LocalDate.now();
        int dayOffset = 0;
        int planDayIdx = 0;
        int createdCount = 0;
        
        // We iterate and map the N days in the plan to the calendar based on chosen weekly training days.
        // If a plan has 28 entries, we place them one by one. 
        // Training entries go to weeklyDays, Rest entries (or others) go to the gaps or are skipped?
        // Standard fitness plan logic: Plan has a sequence of "sessions". 
        // We map sessions to training days.
        
        while (planDayIdx < planDays.size()) {
            LocalDate date = current.plusDays(dayOffset);
            String dayOfWeekName = date.getDayOfWeek().name();
            
            Map<String, Object> dayConfig = planDays.get(planDayIdx);
            String type = (String) dayConfig.get("type");
            
            System.out.println("[ScheduleService] Processing day " + planDayIdx + ", date: " + date + ", dayOfWeek: " + dayOfWeekName + ", type: " + type);

            if ("训练".equals(type)) {
                // Training sessions MUST land on a training day
                if (weeklyDays.contains(dayOfWeekName)) {
                    DailySchedule schedule = new DailySchedule();
                    schedule.setUserId(userPlan.getUserId());
                    schedule.setPlanId(userPlan.getId());
                    schedule.setSourceType("PLAN");
                    schedule.setDate(date);
                    schedule.setStatus("PENDING");
                    schedule.setCreateTime(java.time.LocalDateTime.now());

                    // Resolve Course
                    Object courseIdObj = dayConfig.get("courseId");
                    if (courseIdObj != null) {
                        Integer courseId = null;
                        if (courseIdObj instanceof Number) {
                            courseId = ((Number) courseIdObj).intValue();
                        } else if (courseIdObj instanceof String) {
                            try { courseId = Integer.parseInt((String) courseIdObj); } catch(Exception ignored) {}
                        }
                        
                        if (courseId != null) {
                            com.health.platform.entity.Course course = courseMapper.selectById(courseId);
                            if (course != null) {
                                schedule.setCourseId(courseId);
                                schedule.setTitle(course.getTitle());
                                schedule.setDescription(course.getDescription());
                                schedule.setActions(course.getActionsJson());
                            } else {
                                schedule.setTitle("未知课程训练");
                            }
                        } else {
                            schedule.setTitle((String) dayConfig.get("title"));
                        }
                    } else {
                        schedule.setTitle((String) dayConfig.get("title"));
                    }
                    
                    scheduleMapper.insert(schedule);
                    createdCount++;
                    System.out.println("[ScheduleService] Created schedule: " + schedule.getTitle() + " on " + date);
                    planDayIdx++;
                }
            } else {
                // Rest days or other non-training days just skip the counter or can be explicitly placed
                // If it's a rest day in the plan, we just move to the next plan day without requiring a specific calendar day
                // but usually rest days fill the gaps.
                System.out.println("[ScheduleService] Skipping non-training day: " + type);
                planDayIdx++; 
                // For rest days, we don't necessarily create a DailySchedule unless we want "Rest" tasks.
            }
            
            dayOffset++;
            if (dayOffset > 365) break; // Safety break
        }
        
        System.out.println("[ScheduleService] Schedule generation completed. Created " + createdCount + " schedules");
    }
}
