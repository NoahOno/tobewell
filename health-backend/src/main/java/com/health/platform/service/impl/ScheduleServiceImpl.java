package com.health.platform.service.impl;

import com.health.platform.entity.DailySchedule;
import com.health.platform.entity.TrainingPlan;
import com.health.platform.mapper.DailyScheduleMapper;
import com.health.platform.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private DailyScheduleMapper scheduleMapper;

    @Override
    public void generateSchedule(TrainingPlan userPlan, List<String> weeklyDays) {
        if (weeklyDays == null || weeklyDays.isEmpty()) {
            weeklyDays = List.of("MONDAY", "WEDNESDAY", "FRIDAY"); // Default
        }
        
        // Clear existing future schedules to allow regenerating
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DailySchedule> deleteWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        deleteWrapper.eq("plan_id", userPlan.getId());
        scheduleMapper.delete(deleteWrapper);
        
        int totalDays = 28; // Default 4 weeks
        if (userPlan.getDuration() != null) {
            String dur = userPlan.getDuration().toLowerCase();
            try {
                int num = Integer.parseInt(dur.replaceAll("[^0-9]", ""));
                if (dur.contains("week") || dur.contains("周")) {
                    totalDays = num * 7;
                } else if (dur.contains("day") || dur.contains("天") || dur.contains("日")) {
                    totalDays = num;
                }
            } catch (Exception e) {
                // Ignore parse errors, fallback to 28
            }
        }
        
        LocalDate current = userPlan.getStartDate() != null ? userPlan.getStartDate() : LocalDate.now();
        LocalDate end = current.plusDays(totalDays - 1);
        
        int workoutNumber = 1;
        while (!current.isAfter(end)) {
            String dayName = current.getDayOfWeek().name();
            if (weeklyDays.contains(dayName)) {
                DailySchedule schedule = new DailySchedule();
                schedule.setUserId(userPlan.getUserId());
                schedule.setPlanId(userPlan.getId());
                schedule.setSourceType("PLAN");
                schedule.setDate(current);
                schedule.setTitle(userPlan.getTitle() + " - 第 " + workoutNumber + " 次训练");
                schedule.setDescription(userPlan.getDescription());
                schedule.setActions(userPlan.getActions());
                schedule.setStatus("PENDING");
                scheduleMapper.insert(schedule);
                workoutNumber++;
            }
            current = current.plusDays(1);
        }
    }
}
